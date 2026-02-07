// DTO per la sezione notifiche admin

// Allineato a NotificationDto backend
export interface NotificationItem {
  id: number;
  recipientType: 'DOCTOR' | 'PATIENT' | 'ADMIN';
  recipientId: string;
  channel: 'EMAIL' | 'IN_APP';
  toAddress?: string;
  subject: string;
  body: string;
  status: 'PENDING' | 'SENT' | 'FAILED';
  createdAt: string;
  sentAt?: string;
  errorMessage?: string;
  // Campi calcolati per UI
  recipient?: string;
  message?: string;
}

export interface NotificationPage {
  content: NotificationItem[];
}

// Payload per creazione notifica (allineato a NotificationCreateDto backend)
export interface NotificationCreatePayload {
  recipientType: 'PATIENT' | 'DOCTOR' | 'ADMIN';
  recipientId: string;
  channel: 'EMAIL' | 'IN_APP';
  toAddress?: string;
  subject: string;
  body: string;
}
