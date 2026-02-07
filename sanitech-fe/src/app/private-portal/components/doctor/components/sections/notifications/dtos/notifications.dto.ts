// ============================================================================
// DTOs locali per il componente Notifications
// ============================================================================

export type NotificationType = 'CONSENT' | 'APPOINTMENT' | 'DOCUMENT' | 'ADMISSION' | 'SYSTEM';
export type NotificationStatus = 'UNREAD' | 'READ' | 'ARCHIVED';
export type NotificationPriority = 'NORMAL' | 'HIGH';

export interface Notification {
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

export interface NotificationPreferences {
  emailConsents: boolean;
  emailAppointments: boolean;
  emailDocuments: boolean;
  emailAdmissions: boolean;
}
