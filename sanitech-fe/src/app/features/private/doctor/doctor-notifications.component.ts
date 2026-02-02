import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

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
  showPreferencesModal = false;
  selectedNotification: Notification | null = null;

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

    setTimeout(() => {
      // Mock data - scenario Dott. Greco
      this.notifications = [
        {
          id: 1,
          type: 'CONSENT',
          title: 'Nuovo consenso ricevuto',
          message: 'Il paziente Mario Rossi ti ha concesso l\'accesso ai documenti.',
          createdAt: this.getHoursAgo(1),
          status: 'UNREAD',
          priority: 'NORMAL',
          link: '/portal/doctor/patients',
          actionLabel: 'Vai ai pazienti',
          patientName: 'Mario Rossi'
        },
        {
          id: 2,
          type: 'CONSENT',
          title: 'Consenso revocato',
          message: 'Il paziente Anna Verdi ha revocato il consenso per le prescrizioni. Mantiene attivo il consenso per i documenti.',
          createdAt: this.getHoursAgo(2),
          status: 'UNREAD',
          priority: 'HIGH',
          link: '/portal/doctor/patients',
          actionLabel: 'Visualizza dettagli',
          patientName: 'Anna Verdi'
        },
        {
          id: 3,
          type: 'APPOINTMENT',
          title: 'Nuovo appuntamento prenotato',
          message: 'Sara Conti ha prenotato un appuntamento per domani alle 10:00.',
          createdAt: this.getHoursAgo(3),
          status: 'UNREAD',
          priority: 'NORMAL',
          link: '/portal/doctor/agenda',
          actionLabel: 'Vai all\'agenda',
          patientName: 'Sara Conti'
        },
        {
          id: 4,
          type: 'APPOINTMENT',
          title: 'Appuntamento cancellato',
          message: 'Luigi Bianchi ha cancellato l\'appuntamento del 15/02 alle 14:00.',
          createdAt: this.getHoursAgo(4),
          status: 'UNREAD',
          priority: 'HIGH',
          link: '/portal/doctor/agenda',
          actionLabel: 'Vai all\'agenda',
          patientName: 'Luigi Bianchi'
        },
        {
          id: 5,
          type: 'DOCUMENT',
          title: 'Nuovo documento caricato',
          message: 'Il paziente Francesco Romano ha caricato nuovi esami di laboratorio.',
          createdAt: this.getHoursAgo(6),
          status: 'UNREAD',
          priority: 'NORMAL',
          link: '/portal/doctor/clinical-docs',
          actionLabel: 'Visualizza documenti',
          patientName: 'Francesco Romano'
        },
        {
          id: 6,
          type: 'ADMISSION',
          title: 'Nuovo paziente assegnato',
          message: 'Ti è stato assegnato un nuovo paziente in reparto: Giovanni Greco.',
          createdAt: this.getHoursAgo(8),
          status: 'UNREAD',
          priority: 'HIGH',
          link: '/portal/doctor/admissions',
          actionLabel: 'Visualizza ricovero',
          patientName: 'Giovanni Greco'
        },
        {
          id: 7,
          type: 'SYSTEM',
          title: 'Aggiornamento sistema',
          message: 'È disponibile una nuova versione del portale con miglioramenti alle televisite.',
          createdAt: this.getDaysAgo(1),
          status: 'READ',
          priority: 'NORMAL'
        },
        {
          id: 8,
          type: 'APPOINTMENT',
          title: 'Promemoria televisita',
          message: 'Hai una televisita programmata tra 30 minuti con Giulia Rossi.',
          createdAt: this.getDaysAgo(1),
          status: 'READ',
          priority: 'HIGH',
          link: '/portal/doctor/televisits',
          actionLabel: 'Vai alle televisite',
          patientName: 'Giulia Rossi'
        },
        {
          id: 9,
          type: 'CONSENT',
          title: 'Nuovo consenso ricevuto',
          message: 'Il paziente Elena Colombo ti ha concesso l\'accesso alle televisite.',
          createdAt: this.getDaysAgo(2),
          status: 'READ',
          priority: 'NORMAL',
          patientName: 'Elena Colombo'
        },
        {
          id: 10,
          type: 'DOCUMENT',
          title: 'Referto disponibile',
          message: 'Il laboratorio ha caricato i risultati degli esami per Mario Esposito.',
          createdAt: this.getDaysAgo(3),
          status: 'ARCHIVED',
          priority: 'NORMAL',
          patientName: 'Mario Esposito'
        }
      ];

      this.isLoading = false;
    }, 500);
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
