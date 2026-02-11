import { Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import {
  PatientService,
  TelevisitDto,
  TelevisitStatus
} from '../../../services/patient.service';
import { DoctorDto, DepartmentDto } from '../../../services/scheduling.service';
import { TelevisitWithDetails, DeviceCheck } from './dtos/televisits.dto';

@Component({
  selector: 'app-patient-televisits',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-televisits.component.html'
})
export class PatientTelevisitsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati televisite
  televisits: TelevisitWithDetails[] = [];

  // Dati per arricchimento
  private doctors: DoctorDto[] = [];
  private departments: DepartmentDto[] = [];

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  showDeviceTestModal = false;
  selectedTelevisit: TelevisitWithDetails | null = null;

  // Verifica dispositivi
  deviceCheck: DeviceCheck = {
    camera: 'pending',
    microphone: 'pending',
    connection: 'pending'
  };
  isTestingDevices = false;
  private mediaStream: MediaStream | null = null;
  @ViewChild('videoPreview') videoPreviewRef!: ElementRef<HTMLVideoElement>;

  // Filtri
  statusFilter: 'ALL' | TelevisitStatus = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // Tempo corrente per countdown
  currentTime = new Date();
  private intervalId: ReturnType<typeof setInterval> | null = null;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadTelevisits();
    this.intervalId = setInterval(() => {
      this.currentTime = new Date();
    }, 1000);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    this.stopMediaStream();
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showDeviceTestModal) {
      this.closeDeviceTestModal();
    } else if (this.showDetailModal) {
      this.closeDetailModal();
    }
  }

  loadTelevisits(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      televisits: this.patientService.getTelevisits({ size: 100, sort: 'scheduledAt,desc' }),
      enrichment: this.patientService.loadEnrichmentData()
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: ({ televisits, enrichment }) => {
        this.doctors = enrichment.doctors;
        this.departments = enrichment.departments;
        this.televisits = this.enrichTelevisits(televisits.content);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Errore caricamento televisite:', err);
        this.errorMessage = 'Impossibile caricare le televisite. Riprova.';
        this.isLoading = false;
      }
    });
  }

  private enrichTelevisits(televisits: TelevisitDto[]): TelevisitWithDetails[] {
    const deptMap = new Map(this.departments.map(d => [d.code, d]));

    return televisits.map(t => {
      const dept = deptMap.get(t.department);
      return {
        ...t,
        departmentName: dept?.name || t.department
      };
    });
  }

  get filteredTelevisits(): TelevisitWithDetails[] {
    return this.televisits.filter(t => {
      if (this.statusFilter !== 'ALL' && t.status !== this.statusFilter) return false;
      return true;
    });
  }

  get paginatedTelevisits(): TelevisitWithDetails[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredTelevisits.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTelevisits.length / this.pageSize) || 1;
  }

  get paginationPages(): (number | '...')[] {
    const total = this.totalPages;
    const current = this.currentPage;

    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }

    const pages: (number | '...')[] = [1];

    if (current > 3) {
      pages.push('...');
    }

    const start = Math.max(2, current - 1);
    const end = Math.min(total - 1, current + 1);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    if (current < total - 2) {
      pages.push('...');
    }

    pages.push(total);

    return pages;
  }

  get upcomingTelevisit(): TelevisitWithDetails | null {
    const upcoming = this.televisits
      .filter(t => (t.status === 'CREATED' || t.status === 'SCHEDULED') && new Date(t.scheduledAt) > this.currentTime)
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime());
    return upcoming.length > 0 ? upcoming[0] : null;
  }

  get scheduledCount(): number {
    return this.televisits.filter(t => t.status === 'CREATED' || t.status === 'SCHEDULED').length;
  }

  get completedCount(): number {
    return this.televisits.filter(t => t.status === 'ENDED').length;
  }

  getStatusLabel(status: TelevisitStatus): string {
    const labels: Record<TelevisitStatus, string> = {
      CREATED: 'Creata',
      SCHEDULED: 'Programmata',
      ACTIVE: 'In corso',
      ENDED: 'Completata',
      CANCELED: 'Annullata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: TelevisitStatus): string {
    const classes: Record<TelevisitStatus, string> = {
      CREATED: 'bg-info',
      SCHEDULED: 'bg-primary',
      ACTIVE: 'bg-success',
      ENDED: 'bg-secondary',
      CANCELED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
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
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getCountdown(scheduledAt: string): string {
    const scheduled = new Date(scheduledAt);
    const diff = scheduled.getTime() - this.currentTime.getTime();

    if (diff <= 0) return 'Ora';

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    if (days > 0) return `${days}g ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    return `${minutes} minuti`;
  }

  canJoinVisit(televisit: TelevisitWithDetails): boolean {
    if (televisit.status !== 'CREATED' && televisit.status !== 'SCHEDULED' && televisit.status !== 'ACTIVE') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const tenMinutesBefore = new Date(scheduled.getTime() - 10 * 60000);
    return this.currentTime >= tenMinutesBefore;
  }

  isUpcoming(televisit: TelevisitWithDetails): boolean {
    if (televisit.status !== 'CREATED' && televisit.status !== 'SCHEDULED') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const oneHourFromNow = new Date(this.currentTime.getTime() + 60 * 60000);
    return scheduled <= oneHourFromNow && scheduled > this.currentTime;
  }

  isFinished(televisit: TelevisitWithDetails): boolean {
    return televisit.status === 'ENDED' || televisit.status === 'CANCELED';
  }

  isActive(televisit: TelevisitWithDetails): boolean {
    return televisit.status === 'CREATED' || televisit.status === 'SCHEDULED' || televisit.status === 'ACTIVE';
  }

  openDetailModal(televisit: TelevisitWithDetails): void {
    this.selectedTelevisit = televisit;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedTelevisit = null;
  }

  openDeviceTestModal(): void {
    this.deviceCheck = {
      camera: 'pending',
      microphone: 'pending',
      connection: 'pending'
    };
    this.showDeviceTestModal = true;
  }

  closeDeviceTestModal(): void {
    this.showDeviceTestModal = false;
    this.stopMediaStream();
  }

  testDevices(): void {
    this.isTestingDevices = true;
    this.deviceCheck = {
      camera: 'pending',
      microphone: 'pending',
      connection: 'pending'
    };
    this.stopMediaStream();

    // Test connessione Internet reale
    const connectionPromise = fetch('/assets/favicon.ico', { cache: 'no-store' })
      .then(() => { this.deviceCheck.connection = 'ok'; })
      .catch(() => { this.deviceCheck.connection = 'error'; });

    // Test webcam e microfono reali con getUserMedia
    const mediaPromise = navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      .then((stream) => {
        this.mediaStream = stream;
        this.deviceCheck.camera = 'ok';
        this.deviceCheck.microphone = 'ok';

        // Mostra anteprima video
        setTimeout(() => {
          if (this.videoPreviewRef?.nativeElement && this.mediaStream) {
            this.videoPreviewRef.nativeElement.srcObject = this.mediaStream;
          }
        });
      })
      .catch((err) => {
        const errorName = err?.name || '';
        if (errorName === 'NotAllowedError' || errorName === 'PermissionDeniedError'
            || errorName === 'NotFoundError' || errorName === 'DevicesNotFoundError') {
          this.deviceCheck.camera = 'error';
          this.deviceCheck.microphone = 'error';
          return;
        }
        // Prova solo audio
        return navigator.mediaDevices.getUserMedia({ audio: true })
          .then((audioStream) => {
            this.mediaStream = audioStream;
            this.deviceCheck.microphone = 'ok';
            this.deviceCheck.camera = 'error';
          })
          .catch(() => {
            this.deviceCheck.camera = 'error';
            this.deviceCheck.microphone = 'error';
          });
      });

    Promise.allSettled([connectionPromise, mediaPromise]).then(() => {
      this.isTestingDevices = false;
      const allOk = this.deviceCheck.connection === 'ok'
        && this.deviceCheck.camera === 'ok'
        && this.deviceCheck.microphone === 'ok';
      if (allOk) {
        this.successMessage = 'Tutti i dispositivi funzionano correttamente!';
        setTimeout(() => this.successMessage = '', 3000);
      }
    });
  }

  private stopMediaStream(): void {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
      this.mediaStream = null;
    }
  }

  joinVisit(televisit: TelevisitWithDetails): void {
    this.patientService.getPatientTelevisitToken(televisit.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tokenData) => {
          window.open(tokenData.livekitUrl, '_blank');
          this.successMessage = `Connessione alla sala d'attesa virtuale per la visita con ${televisit.departmentName}...`;
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore generazione token:', err);
          this.errorMessage = 'Impossibile connettersi alla televisita. Riprova.';
        }
      });
  }

  getDeviceCheckIcon(status: 'pending' | 'ok' | 'error'): string {
    switch (status) {
      case 'ok': return 'bi-check-circle-fill text-success';
      case 'error': return 'bi-x-circle-fill text-danger';
      default: return 'bi-circle text-muted';
    }
  }

  getDeviceCheckLabel(status: 'pending' | 'ok' | 'error'): string {
    switch (status) {
      case 'ok': return 'Funzionante';
      case 'error': return 'Errore';
      default: return 'Da testare';
    }
  }
}
