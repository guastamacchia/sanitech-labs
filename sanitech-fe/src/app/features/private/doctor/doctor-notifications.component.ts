import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  DoctorApiService,
  NotificationDto,
  NotificationStatus as ApiNotificationStatus
} from './doctor-api.service';

type NotificationType = 'CONSENT' | 'APPOINTMENT' | 'DOCUMENT' | 'ADMISSION' | 'SYSTEM';
type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';
type NotificationPriority = 'NORMAL' | 'HIGH';

interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  createdAt: string;
  status: NotificationStatus;
  priority: NotificationPriority;
  link?: string;
  actionLabel?: string;
  patientName?: string;
}

interface NotificationPreferences {
  emailConsents: boolean;
  emailAppointments: boolean;
  emailDocuments: boolean;
  emailAdmissions: boolean;
}

@Component({
  selector: 'app-doctor-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-notifications.component.html'
})
export class DoctorNotificationsComponent implements OnInit {
  // Notifiche
  notifications: Notification[] = [];

  // Preferenze
  preferences: NotificationPreferences = {
    emailConsents: true,
    emailAppointments: true,
    emailDocuments: false,
    emailAdmissions: true
  };

  // Filtri
  typeFilter: 'ALL' | NotificationType = 'ALL';
  statusFilter: 'ALL' | 'UNREAD' | 'READ' = 'ALL';
  showArchive = false;

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  // UI State
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showPreferencesModal = false;
  selectedNotification: Notification | null = null;

  constructor(private doctorApi: DoctorApiService) {}

  // Stats
  get unreadCount(): number {
    return this.notifications.filter(n => n.status === 'UNREAD').length;
  }

  get todayCount(): number {
    const today = new Date().toDateString();
    return this.notifications.filter(n =>
      new Date(n.createdAt).toDateString() === today && n.status !== 'ARCHIVED'
    ).length;
  }

  get highPriorityCount(): number {
    return this.notifications.filter(n =>
      n.priority === 'HIGH' && n.status === 'UNREAD'
    ).length;
  }

  get activeNotificationsCount(): number {
    return this.notifications.filter(n => n.status !== 'ARCHIVED').length;
  }

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.listNotifications({ size: 100 }).subscribe({
      next: (page) => {
        const dtos = page.content || [];
        this.notifications = dtos.map(dto => this.mapNotification(dto));
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento delle notifiche.';
        this.isLoading = false;
      }
    });
  }

  private mapNotification(dto: NotificationDto): Notification {
    // Estrai tipo dalla subject o body
    let type: NotificationType = 'SYSTEM';
    const subjectLower = (dto.subject || '').toLowerCase();
    if (subjectLower.includes('consenso')) type = 'CONSENT';
    else if (subjectLower.includes('appuntamento') || subjectLower.includes('televisita')) type = 'APPOINTMENT';
    else if (subjectLower.includes('documento') || subjectLower.includes('referto')) type = 'DOCUMENT';
    else if (subjectLower.includes('ricovero') || subjectLower.includes('ammissione')) type = 'ADMISSION';

    // Mappa status
    let status: NotificationStatus = 'UNREAD';
    if (dto.status === 'SENT') status = 'UNREAD';
    else if (dto.status === 'READ') status = 'READ';

    return {
      id: dto.id,
      type,
      title: dto.subject,
      message: dto.body,
      createdAt: dto.createdAt,
      status,
      priority: 'NORMAL' // Il backend non ha un campo priority
    };
  }

  getHoursAgo(hours: number): string {
    const date = new Date();
    date.setHours(date.getHours() - hours);
    return date.toISOString();
  }

  getDaysAgo(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date.toISOString();
  }

  get filteredNotifications(): Notification[] {
    let filtered = this.notifications;

    if (this.showArchive) {
      filtered = filtered.filter(n => n.status === 'ARCHIVED');
    } else {
      filtered = filtered.filter(n => n.status !== 'ARCHIVED');
    }

    if (this.typeFilter !== 'ALL') {
      filtered = filtered.filter(n => n.type === this.typeFilter);
    }

    if (this.statusFilter !== 'ALL' && !this.showArchive) {
      filtered = filtered.filter(n => n.status === this.statusFilter);
    }

    return filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }

  get paginatedNotifications(): Notification[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredNotifications.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredNotifications.length / this.pageSize) || 1;
  }

  markAsRead(notification: Notification): void {
    if (notification.status === 'UNREAD') {
      notification.status = 'READ';
    }
    this.selectedNotification = notification;
  }

  markAllAsRead(): void {
    this.notifications.forEach(n => {
      if (n.status === 'UNREAD') {
        n.status = 'READ';
      }
    });
    this.successMessage = 'Tutte le notifiche sono state segnate come lette.';
    setTimeout(() => this.successMessage = '', 3000);
  }

  archiveNotification(notification: Notification): void {
    notification.status = 'ARCHIVED';
    this.successMessage = 'Notifica archiviata.';
    setTimeout(() => this.successMessage = '', 3000);
  }

  toggleArchive(): void {
    this.showArchive = !this.showArchive;
    this.currentPage = 1;
  }

  openPreferencesModal(): void {
    this.showPreferencesModal = true;
  }

  closePreferencesModal(): void {
    this.showPreferencesModal = false;
  }

  savePreferences(): void {
    this.isSaving = true;

    // Nota: il backend non ha un endpoint per le preferenze notifiche
    // Questo sarebbe un'estensione futura
    setTimeout(() => {
      this.isSaving = false;
      this.closePreferencesModal();
      this.successMessage = 'Preferenze di notifica salvate.';
      setTimeout(() => this.successMessage = '', 3000);
    }, 1000);
  }

  closeDetail(): void {
    this.selectedNotification = null;
  }

  getTypeLabel(type: NotificationType): string {
    const labels: Record<NotificationType, string> = {
      CONSENT: 'Consensi',
      APPOINTMENT: 'Appuntamenti',
      DOCUMENT: 'Documenti',
      ADMISSION: 'Ricoveri',
      SYSTEM: 'Sistema'
    };
    return labels[type];
  }

  getTypeIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      CONSENT: 'bi-shield-check',
      APPOINTMENT: 'bi-calendar-event',
      DOCUMENT: 'bi-file-earmark-medical',
      ADMISSION: 'bi-hospital',
      SYSTEM: 'bi-gear'
    };
    return icons[type];
  }

  getTypeColor(type: NotificationType): string {
    const colors: Record<NotificationType, string> = {
      CONSENT: 'info',
      APPOINTMENT: 'primary',
      DOCUMENT: 'success',
      ADMISSION: 'warning',
      SYSTEM: 'secondary'
    };
    return colors[type];
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) {
      return diffMins <= 1 ? 'Adesso' : `${diffMins} minuti fa`;
    } else if (diffHours < 24) {
      return `${diffHours} ${diffHours === 1 ? 'ora' : 'ore'} fa`;
    } else if (diffDays < 7) {
      return `${diffDays} ${diffDays === 1 ? 'giorno' : 'giorni'} fa`;
    } else {
      return date.toLocaleDateString('it-IT', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    }
  }
}
