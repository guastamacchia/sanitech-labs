import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type TelevisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

interface Televisit {
  id: number;
  doctorName: string;
  doctorSpecialty: string;
  scheduledAt: string;
  duration: number; // minuti
  status: TelevisitStatus;
  notes?: string;
  preparationChecklist?: string[];
  roomUrl?: string;
  attachments?: { name: string; uploaded: boolean }[];
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
export class PatientTelevisitsComponent implements OnInit {
  // Dati televisite
  televisits: Televisit[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  showDeviceTestModal = false;
  selectedTelevisit: Televisit | null = null;

  // Device check
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

  ngOnInit(): void {
    this.loadTelevisits();
    // Aggiorna il countdown ogni secondo
    setInterval(() => {
      this.currentTime = new Date();
    }, 1000);
  }

  loadTelevisits(): void {
    this.isLoading = true;

    // Dati mock - Scenario di Roberto con televisita imminente
    const now = new Date();
    const thirtyMinutesLater = new Date(now.getTime() + 30 * 60000);

    setTimeout(() => {
      this.televisits = [
        {
          id: 1,
          doctorName: 'Dr. Elena Dermati',
          doctorSpecialty: 'Dermatologia',
          scheduledAt: thirtyMinutesLater.toISOString(),
          duration: 20,
          status: 'SCHEDULED',
          notes: 'Controllo lesione cutanea braccio sinistro',
          preparationChecklist: [
            'Verificare connessione internet stabile',
            'Testare webcam e microfono',
            'Preparare eventuali documenti da condividere',
            'Essere in un ambiente ben illuminato'
          ],
          roomUrl: 'https://televisit.sanitech.it/room/abc123',
          attachments: [
            { name: 'foto_lesione_1.jpg', uploaded: true },
            { name: 'foto_lesione_2.jpg', uploaded: true }
          ]
        },
        {
          id: 2,
          doctorName: 'Dr. Marco Cardioli',
          doctorSpecialty: 'Cardiologia',
          scheduledAt: new Date(now.getTime() + 7 * 24 * 60 * 60000).toISOString(),
          duration: 30,
          status: 'SCHEDULED',
          notes: 'Follow-up terapia antipertensiva'
        },
        {
          id: 3,
          doctorName: 'Dr. Anna Medici',
          doctorSpecialty: 'Medicina generale',
          scheduledAt: new Date(now.getTime() - 3 * 24 * 60 * 60000).toISOString(),
          duration: 15,
          status: 'COMPLETED',
          notes: 'Consulto per sintomi influenzali'
        },
        {
          id: 4,
          doctorName: 'Dr. Luigi Ortopedico',
          doctorSpecialty: 'Ortopedia',
          scheduledAt: new Date(now.getTime() - 14 * 24 * 60 * 60000).toISOString(),
          duration: 25,
          status: 'COMPLETED',
          notes: 'Controllo post-operatorio ginocchio'
        },
        {
          id: 5,
          doctorName: 'Dr. Paolo Pneumologo',
          doctorSpecialty: 'Pneumologia',
          scheduledAt: new Date(now.getTime() - 7 * 24 * 60 * 60000).toISOString(),
          duration: 20,
          status: 'CANCELLED',
          notes: 'Cancellato su richiesta paziente'
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredTelevisits(): Televisit[] {
    return this.televisits.filter(t => {
      if (this.statusFilter !== 'ALL' && t.status !== this.statusFilter) return false;
      return true;
    });
  }

  get paginatedTelevisits(): Televisit[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredTelevisits.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredTelevisits.length / this.pageSize) || 1;
  }

  get upcomingTelevisit(): Televisit | null {
    const upcoming = this.televisits
      .filter(t => t.status === 'SCHEDULED' && new Date(t.scheduledAt) > this.currentTime)
      .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime());
    return upcoming.length > 0 ? upcoming[0] : null;
  }

  get scheduledCount(): number {
    return this.televisits.filter(t => t.status === 'SCHEDULED').length;
  }

  get completedCount(): number {
    return this.televisits.filter(t => t.status === 'COMPLETED').length;
  }

  getStatusLabel(status: TelevisitStatus): string {
    const labels: Record<TelevisitStatus, string> = {
      SCHEDULED: 'Programmata',
      IN_PROGRESS: 'In corso',
      COMPLETED: 'Completata',
      CANCELLED: 'Annullata'
    };
    return labels[status];
  }

  getStatusBadgeClass(status: TelevisitStatus): string {
    const classes: Record<TelevisitStatus, string> = {
      SCHEDULED: 'bg-primary',
      IN_PROGRESS: 'bg-success',
      COMPLETED: 'bg-secondary',
      CANCELLED: 'bg-danger'
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

  canJoinVisit(televisit: Televisit): boolean {
    if (televisit.status !== 'SCHEDULED') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const tenMinutesBefore = new Date(scheduled.getTime() - 10 * 60000);
    return this.currentTime >= tenMinutesBefore;
  }

  isUpcoming(televisit: Televisit): boolean {
    if (televisit.status !== 'SCHEDULED') return false;
    const scheduled = new Date(televisit.scheduledAt);
    const oneHourFromNow = new Date(this.currentTime.getTime() + 60 * 60000);
    return scheduled <= oneHourFromNow && scheduled > this.currentTime;
  }

  openDetailModal(televisit: Televisit): void {
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

  joinVisit(televisit: Televisit): void {
    this.successMessage = `Connessione alla sala d'attesa virtuale per la visita con ${televisit.doctorName}...`;
    setTimeout(() => this.successMessage = '', 5000);
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
