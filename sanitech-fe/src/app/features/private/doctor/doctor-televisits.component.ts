import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type TelevisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

interface Televisit {
  id: number;
  patientId: number;
  patientName: string;
  scheduledAt: string;
  duration: number;
  reason: string;
  status: TelevisitStatus;
  roomUrl?: string;
  notes?: string;
  documents?: { id: number; name: string; type: string }[];
}

@Component({
  selector: 'app-doctor-televisits',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-televisits.component.html'
})
export class DoctorTelevisitsComponent implements OnInit {
  // Televisite
  televisits: Televisit[] = [];

  // Filtri
  statusFilter: 'ALL' | TelevisitStatus = 'ALL';
  dateFilter: 'TODAY' | 'WEEK' | 'ALL' = 'TODAY';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showPreparationModal = false;
  showVideoModal = false;
  selectedTelevisit: Televisit | null = null;

  // Note durante visita
  visitNotes = '';

  // Stats
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
    this.loadTelevisits();
  }

  loadTelevisits(): void {
    this.isLoading = true;

    setTimeout(() => {
      const now = new Date();

      // Mock data - scenario Dott.ssa Neri
      this.televisits = [
        {
          id: 1,
          patientId: 3,
          patientName: 'Bianchi Luigi',
          scheduledAt: this.getTodayAt(16, 0),
          duration: 30,
          reason: 'Controllo post-operatorio ginocchio',
          status: 'SCHEDULED',
          roomUrl: 'https://meet.sanitech.it/room/abc123',
          documents: [
            { id: 1, name: 'RX ginocchio pre-intervento', type: 'image' },
            { id: 2, name: 'Diario del dolore', type: 'document' },
            { id: 3, name: 'Foto cicatrice', type: 'image' }
          ]
        },
        {
          id: 2,
          patientId: 2,
          patientName: 'Verdi Anna',
          scheduledAt: this.getTodayAt(17, 0),
          duration: 30,
          reason: 'Consulto per dolore lombare',
          status: 'SCHEDULED',
          roomUrl: 'https://meet.sanitech.it/room/def456'
        },
        {
          id: 3,
          patientId: 8,
          patientName: 'Conti Sara',
          scheduledAt: this.getTomorrowAt(10, 30),
          duration: 30,
          reason: 'Follow-up terapia',
          status: 'SCHEDULED',
          roomUrl: 'https://meet.sanitech.it/room/ghi789'
        },
        {
          id: 4,
          patientId: 1,
          patientName: 'Esposito Mario',
          scheduledAt: this.getYesterdayAt(15, 0),
          duration: 30,
          reason: 'Controllo pressione',
          status: 'COMPLETED',
          notes: 'Paziente stabile. Pressione nella norma. Proseguire terapia attuale.',
          roomUrl: 'https://meet.sanitech.it/room/jkl012'
        },
        {
          id: 5,
          patientId: 4,
          patientName: 'Rossi Giulia',
          scheduledAt: this.getYesterdayAt(11, 0),
          duration: 30,
          reason: 'Consulto dermatologico',
          status: 'COMPLETED',
          notes: 'Prescritto trattamento topico. Controllo tra 2 settimane.',
          roomUrl: 'https://meet.sanitech.it/room/mno345'
        },
        {
          id: 6,
          patientId: 5,
          patientName: 'Romano Francesco',
          scheduledAt: this.getYesterdayAt(9, 0),
          duration: 30,
          reason: 'Prima visita',
          status: 'NO_SHOW',
          roomUrl: 'https://meet.sanitech.it/room/pqr678'
        }
      ];

      this.isLoading = false;
    }, 500);
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

    this.selectedTelevisit.status = 'IN_PROGRESS';
    this.visitNotes = '';
    this.showPreparationModal = false;
    this.showVideoModal = true;
    this.successMessage = 'Televisita avviata. In attesa che il paziente si colleghi...';
    setTimeout(() => this.successMessage = '', 3000);
  }

  endTelevisit(generateReport: boolean): void {
    if (!this.selectedTelevisit) return;

    this.selectedTelevisit.status = 'COMPLETED';
    this.selectedTelevisit.notes = this.visitNotes;
    this.showVideoModal = false;

    if (generateReport) {
      this.successMessage = 'Televisita conclusa. Referto di visita generato.';
    } else {
      this.successMessage = 'Televisita conclusa.';
    }
    setTimeout(() => this.successMessage = '', 5000);
    this.selectedTelevisit = null;
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
