import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, finalize } from 'rxjs';
import {
  PatientService,
  NotificationDto,
  NotificationChannel,
  NotificationStatus
} from '../../../services/patient.service';

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
  statusFilter: 'ALL' | 'SENT' | 'READ' | 'ARCHIVED' = 'ALL';
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

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.showDetailModal) {
      this.closeDetailModal();
    }
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.patientService.getNotifications({ size: 100, sort: 'createdAt,desc' })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: (response) => {
          this.notifications = response.content;
        },
        error: () => {
          this.errorMessage = 'Impossibile caricare le notifiche. Riprova.';
        }
      });
  }

  // --- Contatori per stats cards (contesto paziente) ---

  get unreadCount(): number {
    return this.notifications.filter(n => n.status === 'SENT').length;
  }

  get readCount(): number {
    return this.notifications.filter(n => n.status === 'READ').length;
  }

  get totalCount(): number {
    return this.notifications.filter(n => n.status !== 'ARCHIVED').length;
  }

  // --- Filtraggio e paginazione ---

  get filteredNotifications(): NotificationDto[] {
    return this.notifications
      .filter(n => {
        if (this.statusFilter !== 'ALL' && n.status !== this.statusFilter) return false;
        if (this.channelFilter !== 'ALL' && n.channel !== this.channelFilter) return false;
        return true;
      })
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  get paginatedNotifications(): NotificationDto[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredNotifications.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredNotifications.length / this.pageSize) || 1;
  }

  // --- Azioni sulle notifiche ---

  markAsRead(notification: NotificationDto): void {
    if (notification.status !== 'SENT') return;

    this.patientService.markNotificationAsRead(notification.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updated) => {
          const idx = this.notifications.findIndex(n => n.id === updated.id);
          if (idx !== -1) this.notifications[idx] = updated;
        },
        error: () => {
          this.errorMessage = 'Errore nel segnare la notifica come letta.';
          this.autoClearError();
        }
      });
  }

  markAllAsRead(): void {
    this.patientService.markAllNotificationsAsRead()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.notifications.forEach(n => {
            if (n.status === 'SENT') {
              (n as { status: NotificationStatus }).status = 'READ';
            }
          });
          this.successMessage = `${result.updated} notifiche segnate come lette.`;
          this.autoClearSuccess();
        },
        error: () => {
          this.errorMessage = 'Errore nel segnare tutte le notifiche come lette.';
          this.autoClearError();
        }
      });
  }

  archiveNotification(notification: NotificationDto, event?: MouseEvent): void {
    if (event) event.stopPropagation();

    this.patientService.archiveNotification(notification.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updated) => {
          const idx = this.notifications.findIndex(n => n.id === updated.id);
          if (idx !== -1) this.notifications[idx] = updated;
          this.successMessage = 'Notifica archiviata.';
          this.autoClearSuccess();
          if (this.selectedNotification?.id === notification.id) {
            this.closeDetailModal();
          }
        },
        error: () => {
          this.errorMessage = 'Errore nell\'archiviazione della notifica.';
          this.autoClearError();
        }
      });
  }

  // --- Modale dettaglio ---

  openDetailModal(notification: NotificationDto): void {
    this.selectedNotification = notification;
    this.showDetailModal = true;
    // Auto mark-as-read all'apertura
    if (notification.status === 'SENT') {
      this.markAsRead(notification);
    }
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedNotification = null;
  }

  onModalBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closeDetailModal();
    }
  }

  // --- Label / icone / badge helpers ---

  getStatusLabel(status: NotificationStatus): string {
    const labels: Record<string, string> = {
      PENDING: 'In attesa',
      SENT: 'Da leggere',
      FAILED: 'Fallita',
      READ: 'Letta',
      ARCHIVED: 'Archiviata'
    };
    return labels[status] || status;
  }

  getStatusBadgeClass(status: NotificationStatus): string {
    const classes: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      SENT: 'bg-primary',
      FAILED: 'bg-danger',
      READ: 'bg-success',
      ARCHIVED: 'bg-secondary'
    };
    return classes[status] || 'bg-secondary';
  }

  getChannelLabel(channel: NotificationChannel): string {
    const labels: Record<string, string> = {
      EMAIL: 'Email',
      IN_APP: 'In-App'
    };
    return labels[channel] || channel;
  }

  getChannelIcon(channel: NotificationChannel): string {
    const icons: Record<string, string> = {
      EMAIL: 'bi-envelope',
      IN_APP: 'bi-bell'
    };
    return icons[channel] || 'bi-bell';
  }

  getChannelBadgeClass(channel: NotificationChannel): string {
    const classes: Record<string, string> = {
      EMAIL: 'bg-primary bg-opacity-10 text-primary',
      IN_APP: 'bg-info bg-opacity-10 text-info'
    };
    return classes[channel] || 'bg-secondary bg-opacity-10 text-secondary';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return minutes <= 1 ? 'Adesso' : `${minutes} minuti fa`;
    }

    if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000));
      return `${hours} ${hours === 1 ? 'ora' : 'ore'} fa`;
    }

    if (diff < 7 * 24 * 60 * 60 * 1000) {
      const days = Math.floor(diff / (24 * 60 * 60 * 1000));
      return `${days} ${days === 1 ? 'giorno' : 'giorni'} fa`;
    }

    return date.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private autoClearSuccess(): void {
    setTimeout(() => this.successMessage = '', 4000);
  }

  private autoClearError(): void {
    setTimeout(() => this.errorMessage = '', 6000);
  }
}
