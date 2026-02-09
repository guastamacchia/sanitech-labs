import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil, finalize } from 'rxjs';
import {
  DoctorApiService,
  NotificationDto
} from '../../../services/doctor-api.service';
import {
  NotificationType,
  NotificationStatus,
  NotificationPriority,
  Notification,
  NotificationPreferences
} from './dtos/notifications.dto';

@Component({
  selector: 'app-doctor-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './doctor-notifications.component.html'
})
export class DoctorNotificationsComponent implements OnInit, OnDestroy {
  // BUG-06: Subscription cleanup via destroy subject
  private destroy$ = new Subject<void>();

  // Notifiche
  notifications: Notification[] = [];

  // Preferenze (BUG-04: salvate in localStorage, nessun endpoint backend disponibile)
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

  // Stato UI
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  showPreferencesModal = false;
  selectedNotification: Notification | null = null;

  constructor(private doctorApi: DoctorApiService) {}

  // Statistiche
  get unreadCount(): number {
    return this.notifications.filter(n => n.status === 'UNREAD').length;
  }

  // BUG-10: Uso UTC per coerenza con date ISO dal backend
  get todayCount(): number {
    const now = new Date();
    const todayUTC = `${now.getUTCFullYear()}-${String(now.getUTCMonth() + 1).padStart(2, '0')}-${String(now.getUTCDate()).padStart(2, '0')}`;
    return this.notifications.filter(n => {
      if (n.status === 'ARCHIVED') return false;
      const created = new Date(n.createdAt);
      const createdUTC = `${created.getUTCFullYear()}-${String(created.getUTCMonth() + 1).padStart(2, '0')}-${String(created.getUTCDate()).padStart(2, '0')}`;
      return createdUTC === todayUTC;
    }).length;
  }

  // BUG-07: Il backend non ha un campo priority, ma lo deriviamo da keyword ad alta urgenza
  get highPriorityCount(): number {
    return this.notifications.filter(n =>
      n.priority === 'HIGH' && n.status === 'UNREAD'
    ).length;
  }

  get activeNotificationsCount(): number {
    return this.notifications.filter(n => n.status !== 'ARCHIVED').length;
  }

  ngOnInit(): void {
    this.loadPreferences();
    this.loadNotifications();
  }

  // BUG-06: Implementazione OnDestroy per cleanup
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // BUG-09: Chiudi modal con Escape
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.selectedNotification) {
      this.closeDetail();
    } else if (this.showPreferencesModal) {
      this.closePreferencesModal();
    }
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.doctorApi.listNotifications({ size: 100 }).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (page) => {
        const dtos = page.content || [];
        this.notifications = dtos.map(dto => this.mapNotification(dto));
      },
      error: () => {
        this.errorMessage = 'Errore nel caricamento delle notifiche. Riprova più tardi.';
      }
    });
  }

  private mapNotification(dto: NotificationDto): Notification {
    // BUG-11: Classificazione tipo migliorata con keyword multiple e categorizzazione più robusta
    const type = this.classifyNotificationType(dto.subject, dto.body);

    // BUG-07: Derivazione priorità da keyword di urgenza
    const priority = this.derivePriority(dto.subject, dto.body);

    // BUG-02: Mappa status backend → status frontend
    let status: NotificationStatus = 'UNREAD';
    if (dto.status === 'SENT') {
      status = 'UNREAD';
    } else if (dto.status === 'READ') {
      status = 'READ';
    } else if (dto.status === 'ARCHIVED') {
      status = 'ARCHIVED';
    }

    // BUG-08: Estrazione patientName e generazione link/actionLabel dal body
    const patientName = this.extractPatientName(dto.body);
    const linkInfo = this.extractLink(type, dto.body);

    return {
      id: dto.id,
      type,
      title: dto.subject,
      message: dto.body,
      createdAt: dto.createdAt,
      status,
      priority,
      patientName,
      link: linkInfo.link,
      actionLabel: linkInfo.actionLabel
    };
  }

  // BUG-11: Classificazione robusta con keyword multiple per subject + body
  private classifyNotificationType(subject: string, body: string): NotificationType {
    const text = `${subject || ''} ${body || ''}`.toLowerCase();

    const patterns: { type: NotificationType; keywords: string[] }[] = [
      { type: 'CONSENT', keywords: ['consenso', 'consensi', 'privacy', 'autorizzazione', 'revoca'] },
      { type: 'APPOINTMENT', keywords: ['appuntamento', 'appuntamenti', 'televisita', 'televisite', 'prenotazione', 'visita', 'slot'] },
      { type: 'DOCUMENT', keywords: ['documento', 'documenti', 'referto', 'referti', 'esame', 'analisi', 'caricamento'] },
      { type: 'ADMISSION', keywords: ['ricovero', 'ricoveri', 'ammissione', 'dimissione', 'reparto', 'degenza'] }
    ];

    for (const pattern of patterns) {
      if (pattern.keywords.some(kw => text.includes(kw))) {
        return pattern.type;
      }
    }
    return 'SYSTEM';
  }

  // BUG-07: Derivazione priorità HIGH per notifiche urgenti
  private derivePriority(subject: string, body: string): NotificationPriority {
    const text = `${subject || ''} ${body || ''}`.toLowerCase();
    const urgentKeywords = [
      'urgente', 'urgenza', 'cancellazione', 'cancellato', 'annullato',
      'revoca', 'revocato', 'immediat', 'critico', 'emergenza'
    ];
    return urgentKeywords.some(kw => text.includes(kw)) ? 'HIGH' : 'NORMAL';
  }

  // BUG-08: Estrazione nome paziente dal body
  private extractPatientName(body: string): string | undefined {
    if (!body) return undefined;
    // Pattern: "Paziente: NomeCognome" o "paziente NomeCognome"
    const match = body.match(/[Pp]aziente[:\s]+([A-ZÀ-Ú][a-zà-ú]+(?:\s[A-ZÀ-Ú][a-zà-ú]+)*)/);
    return match ? match[1] : undefined;
  }

  // BUG-08: Generazione link e actionLabel basata sul tipo
  private extractLink(type: NotificationType, body: string): { link?: string; actionLabel?: string } {
    switch (type) {
      case 'APPOINTMENT':
        return { link: '/portal/doctor/agenda', actionLabel: 'Vai all\'agenda' };
      case 'CONSENT':
        return { link: '/portal/doctor/patients', actionLabel: 'Vedi pazienti' };
      case 'DOCUMENT':
        return { link: '/portal/doctor/clinical-docs', actionLabel: 'Vedi documenti' };
      case 'ADMISSION':
        return { link: '/portal/doctor/admissions', actionLabel: 'Vedi ricoveri' };
      default:
        return {};
    }
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

  // BUG-01: markAsRead ora chiama il backend
  markAsRead(notification: Notification): void {
    if (notification.status === 'UNREAD') {
      this.doctorApi.markNotificationAsRead(notification.id).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: () => {
          notification.status = 'READ';
        },
        error: () => {
          this.errorMessage = 'Errore nel segnare la notifica come letta.';
          this.autoClearError();
        }
      });
    }
    this.selectedNotification = notification;
  }

  // BUG-01: markAllAsRead ora chiama il backend
  markAllAsRead(): void {
    this.doctorApi.markAllNotificationsAsRead().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (result) => {
        this.notifications.forEach(n => {
          if (n.status === 'UNREAD') {
            n.status = 'READ';
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

  // BUG-01: archiveNotification ora chiama il backend
  archiveNotification(notification: Notification): void {
    this.doctorApi.archiveNotification(notification.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: () => {
        notification.status = 'ARCHIVED';
        this.successMessage = 'Notifica archiviata.';
        this.autoClearSuccess();
        // Se era la notifica selezionata nel modal, chiudi
        if (this.selectedNotification?.id === notification.id) {
          this.closeDetail();
        }
      },
      error: () => {
        this.errorMessage = 'Errore nell\'archiviazione della notifica.';
        this.autoClearError();
      }
    });
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

  // BUG-09: Click sul backdrop chiude il modal preferenze
  onPreferencesBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closePreferencesModal();
    }
  }

  // BUG-04: Salvataggio preferenze in localStorage (nessun endpoint backend disponibile)
  savePreferences(): void {
    this.isSaving = true;
    try {
      localStorage.setItem('sanitech_notification_prefs', JSON.stringify(this.preferences));
      this.isSaving = false;
      this.closePreferencesModal();
      this.successMessage = 'Preferenze di notifica salvate localmente.';
      this.autoClearSuccess();
    } catch {
      this.isSaving = false;
      this.errorMessage = 'Errore nel salvataggio delle preferenze.';
      this.autoClearError();
    }
  }

  // BUG-04: Caricamento preferenze da localStorage
  private loadPreferences(): void {
    try {
      const stored = localStorage.getItem('sanitech_notification_prefs');
      if (stored) {
        this.preferences = { ...this.preferences, ...JSON.parse(stored) };
      }
    } catch {
      // Ignora errori di parsing, usa le preferenze di default
    }
  }

  closeDetail(): void {
    this.selectedNotification = null;
  }

  // BUG-09: Click sul backdrop chiude il modal dettaglio
  onDetailBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal')) {
      this.closeDetail();
    }
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

  // BUG-12: Rimossi getHoursAgo e getDaysAgo (dead code)

  private autoClearSuccess(): void {
    setTimeout(() => this.successMessage = '', 4000);
  }

  private autoClearError(): void {
    setTimeout(() => this.errorMessage = '', 6000);
  }
}
