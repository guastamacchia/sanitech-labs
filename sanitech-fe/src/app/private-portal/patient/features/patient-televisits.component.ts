import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import {
  PatientService,
  TelevisitDto,
  TelevisitStatus
} from '../services/patient.service';
import { DoctorDto, DepartmentDto } from '../services/scheduling.service';

interface TelevisitWithDetails extends TelevisitDto {
  doctorName?: string;
  departmentName?: string;
}

interface DeviceCheck {
  camera: 'pending' | 'ok' | 'error';
  microphone: 'pending' | 'ok' | 'error';
  connection: 'pending' | 'ok' | 'error';
}

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

  // Upload foto
  uploadedPhotos: string[] = [];
  showUploadPhotoModal = false;

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
    // Aggiorna il countdown ogni secondo
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

  get upcomingTelevisit(): TelevisitWithDetails | null {
    const upcoming = this.televisits
      .filter(t => t.status === 'CREATED' && new Date(t.scheduledAt) > this.currentTime)
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime());
    return upcoming.length > 0 ? upcoming[0] : null;
  }

  get scheduledCount(): number {
    return this.televisits.filter(t => t.status === 'CREATED').length;
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
    if (televisit.status !== 'CREATED' && televisit.status !== 'ACTIVE') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const tenMinutesBefore = new Date(scheduled.getTime() - 10 * 60000);
    return this.currentTime >= tenMinutesBefore;
  }

  isUpcoming(televisit: TelevisitWithDetails): boolean {
    if (televisit.status !== 'CREATED') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const oneHourFromNow = new Date(this.currentTime.getTime() + 60 * 60000);
    return scheduled <= oneHourFromNow && scheduled > this.currentTime;
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
  }

  testDevices(): void {
    this.isTestingDevices = true;
    this.deviceCheck = {
      camera: 'pending',
      microphone: 'pending',
      connection: 'pending'
    };

    // Simula test dispositivi
    setTimeout(() => {
      this.deviceCheck.connection = 'ok';
    }, 1000);

    setTimeout(() => {
      this.deviceCheck.camera = 'ok';
    }, 2000);

    setTimeout(() => {
      this.deviceCheck.microphone = 'ok';
      this.isTestingDevices = false;
      this.successMessage = 'Tutti i dispositivi funzionano correttamente!';
      setTimeout(() => this.successMessage = '', 3000);
    }, 3000);
  }

  joinVisit(televisit: TelevisitWithDetails): void {
    this.patientService.getPatientTelevisitToken(televisit.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tokenData) => {
          // Apri la stanza LiveKit
          window.open(tokenData.url, '_blank');
          this.successMessage = `Connessione alla sala d'attesa virtuale per la visita con ${televisit.departmentName}...`;
          setTimeout(() => this.successMessage = '', 5000);
        },
        error: (err) => {
          console.error('Errore generazione token:', err);
          this.errorMessage = 'Impossibile connettersi alla televisita. Riprova.';
        }
      });
  }

  openUploadPhotoModal(): void {
    this.showUploadPhotoModal = true;
  }

  closeUploadPhotoModal(): void {
    this.showUploadPhotoModal = false;
  }

  uploadPhoto(): void {
    this.uploadedPhotos.push(`foto_${this.uploadedPhotos.length + 1}.jpg`);
    this.successMessage = 'Foto caricata con successo! Sara\' disponibile durante la visita.';
    setTimeout(() => this.successMessage = '', 3000);
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
