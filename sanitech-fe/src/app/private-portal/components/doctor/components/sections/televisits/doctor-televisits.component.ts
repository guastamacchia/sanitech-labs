import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  DoctorApiService,
  TelevisitDto,
  TelevisitStatus as ApiTelevisitStatus,
  PatientDto
} from '../../../services/doctor-api.service';
import { TelevisitStatus, Televisit } from './dtos/televisits.dto';

@Component({
  selector: 'app-doctor-televisits',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-televisits.component.html'
})
export class DoctorTelevisitsComponent implements OnInit {
  // Televisite
  televisits: Televisit[] = [];

  // Cache pazienti (per subject)
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
  selectedTelevisit: Televisit | null = null;

  // Note durante visita
  visitNotes = '';

  // Statistiche
  get todayVisits(): number {
    const today = new Date().toDateString();
    return this.televisits.filter(t =>
      new Date(t.scheduledAt).toDateString() === today &&
      (t.status === 'SCHEDULED' || t.status === 'IN_PROGRESS')
    ).length;
  }

  get weekVisits(): number {
    const today = new Date();
    const weekEnd = new Date(today);
    weekEnd.setDate(today.getDate() + 7);
    return this.televisits.filter(t =>
      new Date(t.scheduledAt) >= today &&
      new Date(t.scheduledAt) <= weekEnd &&
      t.status === 'SCHEDULED'
    ).length;
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
      // Recupera nome paziente per filtrare le televisite
      this.doctorApi.getPatient(this.patientIdFilter).subscribe({
        next: (patient) => {
          this.patientNameFilter = `${patient.lastName} ${patient.firstName}`;
        }
      });
    }
    this.loadTelevisits();
  }

  loadTelevisits(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.searchTelevisits({ size: 100 }).subscribe({
      next: (page) => {
        const dtos = page.content || [];
        this.televisits = dtos.map(dto => this.mapTelevisit(dto));
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento delle televisite.';
        this.isLoading = false;
      }
    });
  }

  private mapTelevisit(dto: TelevisitDto): Televisit {
    // Il backend usa subject Keycloak, non ID paziente
    // Potremmo fare una lookup, ma per ora usiamo il subject come placeholder
    const patientName = this.patientsCache.get(dto.patientSubject) || `Paziente (${dto.patientSubject.substring(0, 8)}...)`;

    return {
      id: dto.id,
      patientSubject: dto.patientSubject,
      patientName,
      scheduledAt: dto.scheduledAt,
      duration: 30, // Default, il backend potrebbe non avere questo campo
      reason: '-', // Il backend non ha un campo reason nel TelevisitDto
      status: dto.status as TelevisitStatus,
      roomUrl: dto.roomName ? `https://meet.sanitech.it/room/${dto.roomName}` : undefined
    };
  }

  getTodayAt(hours: number, minutes: number): string {
    const date = new Date();
    date.setHours(hours, minutes, 0, 0);
    return date.toISOString();
  }

  getTomorrowAt(hours: number, minutes: number): string {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    date.setHours(hours, minutes, 0, 0);
    return date.toISOString();
  }

  getYesterdayAt(hours: number, minutes: number): string {
    const date = new Date();
    date.setDate(date.getDate() - 1);
    date.setHours(hours, minutes, 0, 0);
    return date.toISOString();
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

    this.doctorApi.startTelevisit(this.selectedTelevisit.id).subscribe({
      next: (updated) => {
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
        this.errorMessage = 'Errore nell\'avvio della televisita.';
      }
    });
  }

  endTelevisit(generateReport: boolean): void {
    if (!this.selectedTelevisit) return;

    this.doctorApi.endTelevisit(this.selectedTelevisit.id).subscribe({
      next: (updated) => {
        if (this.selectedTelevisit) {
          this.selectedTelevisit.status = 'COMPLETED';
          this.selectedTelevisit.notes = this.visitNotes;
        }
        this.showVideoModal = false;
        this.loadTelevisits(); // Ricarica lista

        if (generateReport) {
          this.successMessage = 'Televisita conclusa. Referto di visita generato.';
        } else {
          this.successMessage = 'Televisita conclusa.';
        }
        setTimeout(() => this.successMessage = '', 5000);
        this.selectedTelevisit = null;
      },
      error: () => {
        this.errorMessage = 'Errore nella conclusione della televisita.';
      }
    });
  }

  closeVideoModal(): void {
    if (confirm('Vuoi concludere la televisita?')) {
      this.endTelevisit(false);
    }
  }

  canStart(televisit: Televisit): boolean {
    const now = new Date();
    const scheduledTime = new Date(televisit.scheduledAt);
    const fiveMinutesBefore = new Date(scheduledTime.getTime() - 5 * 60 * 1000);
    return now >= fiveMinutesBefore && televisit.status === 'SCHEDULED';
  }

  getTimeUntil(scheduledAt: string): string {
    const now = new Date();
    const scheduled = new Date(scheduledAt);
    const diff = scheduled.getTime() - now.getTime();

    if (diff < 0) return 'In corso';

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
      CANCELLED: 'Cancellata',
      NO_SHOW: 'Paziente assente'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: TelevisitStatus): string {
    const classes: Record<TelevisitStatus, string> = {
      SCHEDULED: 'bg-primary',
      IN_PROGRESS: 'bg-success',
      COMPLETED: 'bg-secondary',
      CANCELLED: 'bg-danger',
      NO_SHOW: 'bg-warning text-dark'
    };
    return classes[status];
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
