import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {
  PatientService,
  NotificationDto,
  NotificationChannel,
  NotificationStatus
} from '../services/patient.service';

@Component({
  selector: 'app-patient-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-notifications.component.html'
})
export class PatientNotificationsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // Dati notifiche
  notifications: NotificationDto[] = [];

  // Stato UI
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showDetailModal = false;
  selectedNotification: NotificationDto | null = null;

  // Filtri
  statusFilter: 'ALL' | NotificationStatus = 'ALL';
  channelFilter: 'ALL' | NotificationChannel = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  constructor(private patientService: PatientService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.getNotifications({ size: 100, sort: 'createdAt,desc' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.notifications = response.content;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Errore caricamento notifiche:', err);
          this.errorMessage = 'Impossibile caricare le notifiche. Riprova.';
          this.isLoading = false;
        }
      });
  }

  get filteredNotifications(): NotificationDto[] {
    return this.notifications.filter(n => {
      if (this.statusFilter !== 'ALL' && n.status !== this.statusFilter) return false;
      if (this.channelFilter !== 'ALL' && n.channel !== this.channelFilter) return false;
      return true;
    });
  }

  get paginatedNotifications(): NotificationDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredNotifications.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredNotifications.length / this.pageSize) || 1;
  }

  get pendingCount(): number {
    return this.notifications.filter(n => n.status === 'PENDING').length;
  }

  get sentCount(): number {
    return this.notifications.filter(n => n.status === 'SENT').length;
  }

  get failedCount(): number {
    return this.notifications.filter(n => n.status === 'FAILED' || n.status === 'BOUNCED').length;
  }

  get totalCount(): number {
    return this.notifications.length;
  }

  getStatusLabel(status: NotificationStatus): string {
    const labels: Record<NotificationStatus, string> = {
      PENDING: 'In attesa',
      SENT: 'Inviata',
      FAILED: 'Fallita',
      BOUNCED: 'Respinta'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: NotificationStatus): string {
    const classes: Record<NotificationStatus, string> = {
      PENDING: 'bg-warning text-dark',
      SENT: 'bg-success',
      FAILED: 'bg-danger',
      BOUNCED: 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
  }

  getChannelLabel(channel: NotificationChannel): string {
    const labels: Record<NotificationChannel, string> = {
      EMAIL: 'Email',
      SMS: 'SMS',
      PUSH: 'Push'
    };
    return labels[channel] || channel;
  }

  getChannelIcon(channel: NotificationChannel): string {
    const icons: Record<NotificationChannel, string> = {
      EMAIL: 'bi-envelope',
      SMS: 'bi-phone',
      PUSH: 'bi-bell'
    };
    return icons[channel] || 'bi-bell';
  }

  getChannelBadgeClass(channel: NotificationChannel): string {
    const classes: Record<NotificationChannel, string> = {
      EMAIL: 'bg-primary bg-opacity-10 text-primary',
      SMS: 'bg-success bg-opacity-10 text-success',
      PUSH: 'bg-info bg-opacity-10 text-info'
    };
    return classes[channel] || 'bg-secondary bg-opacity-10 text-secondary';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    // Meno di 1 ora
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return minutes <= 1 ? 'Adesso' : `${minutes} minuti fa`;
    }

    // Meno di 24 ore
    if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000));
      return `${hours} ${hours === 1 ? 'ora' : 'ore'} fa`;
    }

    // Meno di 7 giorni
    if (diff < 7 * 24 * 60 * 60 * 1000) {
      const days = Math.floor(diff / (24 * 60 * 60 * 1000));
      return `${days} ${days === 1 ? 'giorno' : 'giorni'} fa`;
    }

    // Altrimenti data completa
    return date.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  openDetailModal(notification: NotificationDto): void {
    this.selectedNotification = notification;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedNotification = null;
  }
}
