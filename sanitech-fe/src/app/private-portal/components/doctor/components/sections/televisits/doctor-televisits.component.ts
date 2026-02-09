import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import {
  DoctorApiService,
  TelevisitDto,
  PatientDto
} from '../../../services/doctor-api.service';
import { TelevisitStatus, Televisit, mapApiStatus } from './dtos/televisits.dto';

@Component({
  selector: 'app-doctor-televisits',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-televisits.component.html'
})
export class DoctorTelevisitsComponent implements OnInit, OnDestroy {
  // Cleanup subscriptions
  private destroy$ = new Subject<void>();

  // Televisite
  televisits: Televisit[] = [];

  // Cache pazienti (per subject → nome completo)
  private patientsCache = new Map<string, string>();

  // Filtro paziente da queryParam
  patientIdFilter = 0;
  patientNameFilter = '';

  constructor(private doctorApi: DoctorApiService, private route: ActivatedRoute) {}

  // Filtri
  statusFilter: 'ALL' | TelevisitStatus = 'ALL';
  dateFilter: 'TODAY' | 'WEEK' | 'ALL' = 'TODAY';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showPreparationModal = false;
  showVideoModal = false;
  showEndConfirmModal = false;
  selectedTelevisit: Televisit | null = null;

  // Note durante visita
  visitNotes = '';

  // Media state (placeholder per futura integrazione LiveKit)
  isMuted = false;
  isCameraOff = false;

  // Statistiche — FIX BUG-02, BUG-17: conteggi coerenti e corretti
  get todayVisits(): number {
    const today = new Date().toDateString();
    return this.televisits.filter(t =>
      new Date(t.scheduledAt).toDateString() === today &&
      (t.status === 'SCHEDULED' || t.status === 'IN_PROGRESS')
    ).length;
  }

  get weekVisits(): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0); // FIX BUG-17: normalizzazione a mezzanotte
    const weekEnd = new Date(today);
    weekEnd.setDate(today.getDate() + 7);
    return this.televisits.filter(t => {
      const date = new Date(t.scheduledAt);
      return date >= today && date <= weekEnd &&
        (t.status === 'SCHEDULED' || t.status === 'IN_PROGRESS'); // FIX BUG-17: stessi stati di todayVisits
    }).length;
  }

  get completedVisits(): number {
    return this.televisits.filter(t => t.status === 'COMPLETED').length;
  }

  get nextVisit(): Televisit | null {
    const now = new Date();
    return this.televisits
      .filter(t => new Date(t.scheduledAt) >= now && t.status === 'SCHEDULED')
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())[0] || null;
  }

  ngOnInit(): void {
    const patientIdParam = this.route.snapshot.queryParamMap.get('patientId');
    if (patientIdParam) {
      this.patientIdFilter = +patientIdParam;
      this.doctorApi.getPatient(this.patientIdFilter).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (patient) => {
          this.patientNameFilter = `${patient.lastName} ${patient.firstName}`;
        }
      });
    }
    this.loadTelevisits();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // FIX BUG-20/G2: Gestione tasto Escape per chiudere modali
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showEndConfirmModal) {
      this.showEndConfirmModal = false;
    } else if (this.showVideoModal) {
      this.showEndConfirmModal = true;
    } else if (this.showPreparationModal) {
      this.closePreparation();
    }
  }

  loadTelevisits(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.searchTelevisits({ size: 100 }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (page) => {
        const dtos = page.content || [];
        // FIX BUG-07: Raccogliere i subject unici per risoluzione nomi
        const uniqueSubjects = [...new Set(dtos.map(d => d.patientSubject))];
        this.resolvePatientNames(uniqueSubjects, dtos);
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento delle televisite. Premi Aggiorna per riprovare.';
        this.isLoading = false;
      }
    });
  }

  /**
   * FIX BUG-07: Risolvi i nomi dei pazienti dai subject Keycloak.
   * Cerca i pazienti per email (subject) e popola la cache.
   */
  private resolvePatientNames(subjects: string[], dtos: TelevisitDto[]): void {
    // Per ogni subject non ancora in cache, cerchiamo il paziente
    const lookups = subjects
      .filter(s => !this.patientsCache.has(s))
      .map(subject =>
        this.doctorApi.searchPatients({ q: subject, size: 1 }).pipe(
          catchError(() => of({ content: [] as PatientDto[], totalElements: 0, totalPages: 0 }))
        )
      );

    if (lookups.length === 0) {
      this.televisits = dtos.map(dto => this.mapTelevisit(dto));
      this.currentPage = 1; // FIX BUG-15: reset paginazione
      this.isLoading = false;
      return;
    }

    forkJoin(lookups).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (results) => {
        const subjectsToResolve = subjects.filter(s => !this.patientsCache.has(s));
        results.forEach((page, index) => {
          const subject = subjectsToResolve[index];
          if (page.content && page.content.length > 0) {
            const p = page.content[0];
            this.patientsCache.set(subject, `${p.lastName} ${p.firstName}`);
          }
        });
        this.televisits = dtos.map(dto => this.mapTelevisit(dto));
        this.currentPage = 1; // FIX BUG-15: reset paginazione
        this.isLoading = false;
      },
      error: () => {
        // Fallback: usa i subject grezzi
        this.televisits = dtos.map(dto => this.mapTelevisit(dto));
        this.currentPage = 1;
        this.isLoading = false;
      }
    });
  }

  private mapTelevisit(dto: TelevisitDto): Televisit {
    // FIX BUG-07: Priorità nomi: 1) backend patientName, 2) cache locale, 3) subject fallback
    const patientName = dto.patientName
      || this.patientsCache.get(dto.patientSubject)
      || this.formatSubjectFallback(dto.patientSubject);

    return {
      id: dto.id,
      patientSubject: dto.patientSubject,
      patientName,
      scheduledAt: dto.scheduledAt,
      duration: 30,
      reason: dto.department || '-',   // FIX BUG-19: mostra il dipartimento come informazione utile
      status: mapApiStatus(dto.status), // FIX BUG-01: conversione stato corretta
      roomUrl: dto.roomName ? `https://meet.sanitech.it/room/${dto.roomName}` : undefined,
      notes: dto.notes
    };
  }

  /**
   * FIX BUG-07: Fallback sicuro per subject — gestisce null/undefined e lunghezze variabili
   */
  private formatSubjectFallback(subject: string | null | undefined): string {
    if (!subject) return 'Paziente sconosciuto';
    const display = subject.length > 20 ? subject.substring(0, 20) + '...' : subject;
    return display;
  }

  get filteredTelevisits(): Televisit[] {
    let filtered = this.televisits;

    // Filtro per paziente da queryParam
    if (this.patientNameFilter) {
      filtered = filtered.filter(t => t.patientName === this.patientNameFilter);
    }

    if (this.statusFilter !== 'ALL') {
      filtered = filtered.filter(t => t.status === this.statusFilter);
    }

    if (this.dateFilter === 'TODAY') {
      const today = new Date().toDateString();
      filtered = filtered.filter(t => new Date(t.scheduledAt).toDateString() === today);
    } else if (this.dateFilter === 'WEEK') {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const weekEnd = new Date(today);
      weekEnd.setDate(today.getDate() + 7);
      filtered = filtered.filter(t => {
        const date = new Date(t.scheduledAt);
        return date >= today && date <= weekEnd;
      });
    }

    return filtered.sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime());
  }

  get paginatedTelevisits(): Televisit[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredTelevisits.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTelevisits.length / this.pageSize) || 1;
  }

  openPreparation(televisit: Televisit): void {
    this.selectedTelevisit = televisit;
    this.showPreparationModal = true;
  }

  closePreparation(): void {
    this.showPreparationModal = false;
    this.selectedTelevisit = null;
  }

  startTelevisit(): void {
    if (!this.selectedTelevisit) return;

    this.errorMessage = ''; // FIX BUG-F2: pulisci errore prima della chiamata
    this.doctorApi.startTelevisit(this.selectedTelevisit.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        if (this.selectedTelevisit) {
          this.selectedTelevisit.status = 'IN_PROGRESS';
        }
        this.visitNotes = '';
        this.showPreparationModal = false;
        this.showVideoModal = true;
        this.successMessage = 'Televisita avviata. In attesa che il paziente si colleghi...';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.errorMessage = 'Errore nell\'avvio della televisita. Riprova.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  endTelevisit(generateReport: boolean): void {
    if (!this.selectedTelevisit) return;

    this.errorMessage = '';
    const televisitId = this.selectedTelevisit.id;

    // FIX BUG-09: Salva le note sul backend prima di concludere, se presenti
    const saveNotes$ = (generateReport && this.visitNotes.trim())
      ? this.doctorApi.updateTelevisitNotes(televisitId, this.visitNotes).pipe(
          catchError(() => of(null)) // Non bloccare la chiusura se il salvataggio note fallisce
        )
      : of(null);

    saveNotes$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.doctorApi.endTelevisit(televisitId).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: () => {
          if (this.selectedTelevisit) {
            this.selectedTelevisit.status = 'COMPLETED';
            this.selectedTelevisit.notes = this.visitNotes;
          }
          this.showVideoModal = false;
          this.showEndConfirmModal = false;
          this.loadTelevisits();

          if (generateReport) {
            this.successMessage = 'Televisita conclusa. Le note sono state salvate.';
          } else {
            this.successMessage = 'Televisita conclusa.';
          }
          setTimeout(() => this.successMessage = '', 5000);
          this.selectedTelevisit = null;
        },
        error: () => {
          this.errorMessage = 'Errore nella conclusione della televisita. Riprova.';
          setTimeout(() => this.errorMessage = '', 5000);
        }
      });
    });
  }

  // FIX BUG-14: Sostituito confirm() con modale custom
  closeVideoModal(): void {
    this.showEndConfirmModal = true;
  }

  cancelEndConfirm(): void {
    this.showEndConfirmModal = false;
  }

  confirmEndTelevisit(): void {
    this.endTelevisit(false);
  }

  canStart(televisit: Televisit): boolean {
    const now = new Date();
    const scheduledTime = new Date(televisit.scheduledAt);
    const fifteenMinutesBefore = new Date(scheduledTime.getTime() - 15 * 60 * 1000);
    // FIX BUG-04: accetta anche IN_PROGRESS per rientrare in una sessione
    return now >= fifteenMinutesBefore && (televisit.status === 'SCHEDULED' || televisit.status === 'IN_PROGRESS');
  }

  getTimeUntil(scheduledAt: string): string {
    const now = new Date();
    const scheduled = new Date(scheduledAt);
    const diff = scheduled.getTime() - now.getTime();

    if (diff < 0) return 'Ora di inizio superata';

    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);

    if (hours > 0) {
      return `Tra ${hours}h ${minutes % 60}m`;
    }
    return `Tra ${minutes}m`;
  }

  getStatusLabel(status: TelevisitStatus): string {
    const labels: Record<TelevisitStatus, string> = {
      SCHEDULED: 'Programmata',
      IN_PROGRESS: 'In corso',
      COMPLETED: 'Completata',
      CANCELLED: 'Cancellata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: TelevisitStatus): string {
    const classes: Record<TelevisitStatus, string> = {
      SCHEDULED: 'bg-primary',
      IN_PROGRESS: 'bg-success',
      COMPLETED: 'bg-secondary',
      CANCELLED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  // FIX BUG-20/G9: Icona differenziata per stato (non solo colore)
  getStatusIcon(status: TelevisitStatus): string {
    const icons: Record<TelevisitStatus, string> = {
      SCHEDULED: 'bi-calendar-check',
      IN_PROGRESS: 'bi-camera-video-fill',
      COMPLETED: 'bi-check-circle-fill',
      CANCELLED: 'bi-x-circle-fill'
    };
    return icons[status] || 'bi-camera-video';
  }

  // FIX BUG-16: Toggle mute/camera (placeholder)
  toggleMute(): void {
    this.isMuted = !this.isMuted;
  }

  toggleCamera(): void {
    this.isCameraOff = !this.isCameraOff;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  formatTime(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleTimeString('it-IT', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isToday(dateStr: string): boolean {
    return new Date(dateStr).toDateString() === new Date().toDateString();
  }
}
