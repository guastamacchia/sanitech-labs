import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type NotificationType = 'APPOINTMENT' | 'DOCUMENT' | 'CONSENT' | 'PAYMENT' | 'SYSTEM';
type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';

interface Notification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  status: NotificationStatus;
  createdAt: string;
  link?: string;
  actionLabel?: string;
}

@Component({
  selector: 'app-patient-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './patient-notifications.component.html'
})
export class PatientNotificationsComponent implements OnInit {
  // Dati notifiche
  notifications: Notification[] = [];

  // UI State
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showArchive = false;

  // Filtri
  typeFilter: 'ALL' | NotificationType = 'ALL';
  statusFilter: 'ALL' | 'UNREAD' | 'READ' = 'ALL';

  // Paginazione
  pageSize = 10;
  currentPage = 1;

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;

    // Dati mock - Scenario di Andrea
    setTimeout(() => {
      this.notifications = [
        {
          id: 1,
          type: 'APPOINTMENT',
          title: 'Promemoria appuntamento domani',
          message: 'Hai un appuntamento domani alle 10:30 con l\'ortopedico Dr. Rossi. Ricorda di portare le radiografie.',
          status: 'UNREAD',
          createdAt: new Date().toISOString(),
          link: '/portal/patient/scheduling',
          actionLabel: 'Vedi appuntamento'
        },
        {
          id: 2,
          type: 'DOCUMENT',
          title: 'Nuovo documento disponibile',
          message: 'Il referto delle analisi del sangue e\' stato aggiunto alla tua cartella clinica.',
          status: 'UNREAD',
          createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
          link: '/portal/patient/docs',
          actionLabel: 'Vedi documento'
        },
        {
          id: 3,
          type: 'CONSENT',
          title: 'Accesso ai tuoi documenti',
          message: 'Il Dr. Bianchi (Cardiologia) ha consultato la tua cartella clinica in data odierna.',
          status: 'UNREAD',
          createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
          link: '/portal/patient/consents',
          actionLabel: 'Gestisci consensi'
        },
        {
          id: 4,
          type: 'PAYMENT',
          title: 'Pagamento ticket in sospeso',
          message: 'Hai un pagamento di 36,15 in sospeso per la visita specialistica del 15/01. Scadenza: 30/01.',
          status: 'READ',
          createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
          link: '/portal/patient/payments',
          actionLabel: 'Paga ora'
        },
        {
          id: 5,
          type: 'APPOINTMENT',
          title: 'Appuntamento confermato',
          message: 'Il tuo appuntamento con il dermatologo e\' stato confermato per il 05/02 alle 14:00.',
          status: 'READ',
          createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString()
        },
        {
          id: 6,
          type: 'DOCUMENT',
          title: 'Documento visualizzato dal medico',
          message: 'Il Dr. Verdi ha visualizzato il documento "Esami del sangue" che hai caricato.',
          status: 'READ',
          createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
        },
        {
          id: 7,
          type: 'SYSTEM',
          title: 'Aggiornamento profilo completato',
          message: 'Le modifiche al tuo profilo sono state salvate con successo.',
          status: 'READ',
          createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
        },
        {
          id: 8,
          type: 'CONSENT',
          title: 'Consenso in scadenza',
          message: 'Il consenso concesso al Dr. Martini scade tra 7 giorni. Vuoi rinnovarlo?',
          status: 'READ',
          createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString(),
          link: '/portal/patient/consents',
          actionLabel: 'Rinnova consenso'
        },
        {
          id: 9,
          type: 'PAYMENT',
          title: 'Pagamento ricevuto',
          message: 'Abbiamo ricevuto il pagamento di 45,00 per la visita cardiologica. Ricevuta disponibile.',
          status: 'ARCHIVED',
          createdAt: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString(),
          link: '/portal/patient/payments',
          actionLabel: 'Scarica ricevuta'
        },
        {
          id: 10,
          type: 'APPOINTMENT',
          title: 'Televisita completata',
          message: 'La televisita con il Dr. Neri e\' stata completata. Il referto sara\' disponibile a breve.',
          status: 'ARCHIVED',
          createdAt: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString()
        }
      ];
      this.isLoading = false;
    }, 500);
  }

  get filteredNotifications(): Notification[] {
    return this.notifications.filter(n => {
      // Filtra per archivio
      if (!this.showArchive && n.status === 'ARCHIVED') return false;
      if (this.showArchive && n.status !== 'ARCHIVED') return false;

      // Filtra per tipo
      if (this.typeFilter !== 'ALL' && n.type !== this.typeFilter) return false;

      // Filtra per stato (solo per non archiviate)
      if (!this.showArchive && this.statusFilter !== 'ALL') {
        if (this.statusFilter === 'UNREAD' && n.status !== 'UNREAD') return false;
        if (this.statusFilter === 'READ' && n.status !== 'READ') return false;
      }

      return true;
    });
  }

  get paginatedNotifications(): Notification[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredNotifications.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredNotifications.length / this.pageSize) || 1;
  }

  get unreadCount(): number {
    return this.notifications.filter(n => n.status === 'UNREAD').length;
  }

  get todayCount(): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return this.notifications.filter(n => {
      const notifDate = new Date(n.createdAt);
      notifDate.setHours(0, 0, 0, 0);
      return notifDate.getTime() === today.getTime() && n.status !== 'ARCHIVED';
    }).length;
  }

  get activeNotificationsCount(): number {
    return this.notifications.filter(n => n.status !== 'ARCHIVED').length;
  }

  getTypeLabel(type: NotificationType): string {
    const labels: Record<NotificationType, string> = {
      APPOINTMENT: 'Appuntamenti',
      DOCUMENT: 'Documenti',
      CONSENT: 'Consensi',
      PAYMENT: 'Pagamenti',
      SYSTEM: 'Sistema'
    };
    return labels[type];
  }

  getTypeIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      APPOINTMENT: 'bi-calendar-check',
      DOCUMENT: 'bi-file-earmark-medical',
      CONSENT: 'bi-shield-check',
      PAYMENT: 'bi-credit-card',
      SYSTEM: 'bi-gear'
    };
    return icons[type];
  }

  getTypeColor(type: NotificationType): string {
    const colors: Record<NotificationType, string> = {
      APPOINTMENT: 'primary',
      DOCUMENT: 'success',
      CONSENT: 'info',
      PAYMENT: 'warning',
      SYSTEM: 'secondary'
    };
    return colors[type];
  }

  formatDate(dateStr: string): string {
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
      year: 'numeric'
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.status === 'UNREAD') {
      notification.status = 'READ';
    }
  }

  markAllAsRead(): void {
    this.notifications.forEach(n => {
      if (n.status === 'UNREAD') {
        n.status = 'READ';
      }
    });
    this.successMessage = 'Tutte le notifiche sono state marcate come lette.';
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
}
