import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { NotificationItem, NotificationPage, NotificationCreatePayload } from './dtos/notifications.dto';

@Injectable({ providedIn: 'root' })
export class NotificationsService {

  constructor(private api: ApiService) {}

  // — Caricamento notifiche —

  loadNotifications(): Observable<NotificationItem[]> {
    return this.api.request<NotificationItem[] | NotificationPage>('GET', '/api/admin/notifications').pipe(
      map(response => {
        const rawNotifications = Array.isArray(response) ? response : (response?.content ?? []);
        return rawNotifications.map(n => this.mapNotificationFromBackend(n));
      })
    );
  }

  // — Invio notifica —

  submitNotification(payload: NotificationCreatePayload): Observable<NotificationItem> {
    return this.api.request<NotificationItem>('POST', '/api/admin/notifications', payload).pipe(
      map(notification => this.mapNotificationFromBackend(notification))
    );
  }

  // — Mappatura backend → UI —

  private mapNotificationFromBackend(n: NotificationItem): NotificationItem {
    return { ...n, recipient: n.toAddress || n.recipientId, message: n.body || n.message };
  }
}
