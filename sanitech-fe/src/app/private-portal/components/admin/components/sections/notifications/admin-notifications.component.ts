import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NotificationsService } from './notifications.service';
import { NotificationItem, NotificationCreatePayload } from './dtos/notifications.dto';

@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-notifications.component.html'
})
export class AdminNotificationsComponent implements OnInit {

  // Stato
  isLoading = false;
  notifications: NotificationItem[] = [];
  notificationsError = '';
  notificationsSuccess = '';

  // Modali
  showAdminNotificationModal = false;
  showNotificationDetailModal = false;
  selectedNotification: NotificationItem | null = null;

  // Form nuova notifica
  notificationForm = {
    recipient: '',
    channel: 'EMAIL',
    recipientType: 'PATIENT' as 'PATIENT' | 'DOCTOR' | 'ADMIN',
    subject: '',
    message: '',
    notes: ''
  };

  // Paginazione
  pageSize = 10;
  pageSizeOptions = [5, 10, 20, 50];
  currentPage = 1;

  constructor(private notificationsService: NotificationsService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  // -- Caricamento dati --

  loadNotifications(): void {
    this.isLoading = true;
    this.notificationsError = '';
    this.notificationsService.loadNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: () => {
        this.notificationsError = 'Impossibile caricare le notifiche.';
        this.isLoading = false;
      }
    });
  }

  // -- Invio notifica --

  submitNotification(): void {
    if (
      !this.notificationForm.recipient.trim() ||
      !this.notificationForm.subject.trim() ||
      !this.notificationForm.message.trim()
    ) {
      this.notificationsError = 'Inserisci destinatario, oggetto e messaggio.';
      return;
    }
    // Validazione formato email se canale EMAIL
    if (this.notificationForm.channel === 'EMAIL') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.notificationForm.recipient.trim())) {
        this.notificationsError = 'Inserisci un indirizzo email valido.';
        return;
      }
    }
    this.isLoading = true;
    this.notificationsError = '';

    const payload: NotificationCreatePayload = {
      recipientType: this.notificationForm.recipientType,
      recipientId: this.notificationForm.recipient,
      channel: this.notificationForm.channel === 'EMAIL' ? 'EMAIL' : 'IN_APP',
      toAddress: this.notificationForm.channel === 'EMAIL' ? this.notificationForm.recipient : undefined,
      subject: this.notificationForm.subject,
      body: this.notificationForm.message
    };

    this.notificationsService.submitNotification(payload).subscribe({
      next: (notification) => {
        this.notifications = [notification, ...this.notifications];
        this.resetNotificationForm();
        if (this.showAdminNotificationModal) {
          this.closeAdminNotificationModal();
        }
        this.isLoading = false;
      },
      error: (err) => {
        const errorMsg = err?.error?.message || err?.error?.error || 'Impossibile inviare la notifica.';
        this.notificationsError = errorMsg;
        this.isLoading = false;
      }
    });
  }

  private resetNotificationForm(): void {
    this.notificationForm = {
      recipient: '',
      channel: 'EMAIL',
      recipientType: 'PATIENT',
      subject: '',
      message: '',
      notes: ''
    };
  }

  // -- Gestione modali --

  openAdminNotificationModal(): void {
    this.notificationsError = '';
    this.notificationsSuccess = '';
    this.resetNotificationForm();
    this.showAdminNotificationModal = true;
  }

  closeAdminNotificationModal(): void {
    this.showAdminNotificationModal = false;
    this.notificationsError = '';
  }

  openNotificationDetailModal(notification: NotificationItem): void {
    this.selectedNotification = notification;
    this.showNotificationDetailModal = true;
  }

  closeNotificationDetailModal(): void {
    this.showNotificationDetailModal = false;
    this.selectedNotification = null;
  }

  // -- Helpers UI --

  getNotificationStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING: 'In coda',
      SENT: 'Inviata',
      DELIVERED: 'Consegnata',
      FAILED: 'Non consegnata'
    };
    return labels[status] ?? status;
  }

  truncateText(text: string | undefined, maxLength: number = 50): string {
    if (!text) return '-';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  }

  formatDateTime(value: string): string {
    if (!value) {
      return '-';
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    const day = String(parsed.getDate()).padStart(2, '0');
    const month = String(parsed.getMonth() + 1).padStart(2, '0');
    const year = parsed.getFullYear();
    const hours = String(parsed.getHours()).padStart(2, '0');
    const minutes = String(parsed.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }

  // -- Contatori statistiche --

  get notificationsSentCount(): number {
    return this.notifications.filter((n) => n.status === 'SENT').length;
  }

  get notificationsPendingCount(): number {
    return this.notifications.filter((n) => n.status === 'PENDING').length;
  }

  get notificationsFailedCount(): number {
    return this.notifications.filter((n) => n.status === 'FAILED').length;
  }

  // -- Paginazione --

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.notifications.length / this.pageSize));
  }

  get sliceStart(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  get sliceEnd(): number {
    return this.currentPage * this.pageSize;
  }

  setPage(page: number): void {
    this.currentPage = Math.min(Math.max(1, page), this.totalPages);
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }
}
