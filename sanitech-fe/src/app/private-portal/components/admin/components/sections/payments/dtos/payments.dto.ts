// DTO per la sezione pagamenti admin
import { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse } from '../../../../dtos/admin-shared.dto';

// Re-export per comodita'
export { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse };

// Allineato a PaymentOrderDto backend
export interface PaymentItem {
  id: number;
  appointmentId: number;
  patientId: number;
  patientEmail?: string;
  patientName?: string;
  amountCents: number;
  currency: string;
  method: 'CARD' | 'BANK_TRANSFER' | 'CASH';
  provider: string;
  providerReference?: string;
  status: 'CREATED' | 'CAPTURED' | 'FAILED' | 'CANCELLED' | 'REFUNDED';
  description?: string;
  createdAt: string;
  updatedAt?: string;
  amount?: number;
  service?: string;
  notificationAttempts?: { sentAt: string }[];
}

export interface PaymentStats {
  totalPayments: number;
  completedWithin7Days: number;
  completedWithReminder: number;
  stillPending: number;
  percentWithin7Days: number;
  percentWithReminder: number;
  percentPending: number;
}

export interface PaymentPage {
  content: PaymentItem[];
}

// Allineato a ServicePerformedDto backend
export type PaymentTypeEnum = 'VISITA' | 'RICOVERO' | 'ALTRO';

export interface ServicePerformedItem {
  id: number;
  serviceType: 'MEDICAL_VISIT' | 'HOSPITALIZATION';
  paymentType?: PaymentTypeEnum;
  sourceType: 'TELEVISIT' | 'ADMISSION';
  sourceId: number;
  patientId: number;
  patientSubject?: string;
  patientName?: string;
  patientEmail?: string;
  doctorId?: number;
  doctorName?: string;
  departmentCode?: string;
  description?: string;
  amountCents: number;
  currency: string;
  status: 'PENDING' | 'PAID' | 'FREE' | 'CANCELLED';
  performedAt: string;
  startedAt?: string;
  daysCount?: number;
  reminderCount: number;
  lastReminderAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
  amount?: number;
}

export interface ServicePerformedStats {
  totalServices: number;
  paidWithin7Days: number;
  paidWithReminder: number;
  stillPending: number;
  percentWithin7Days: number;
  percentWithReminder: number;
  percentPending: number;
  filteredCount: number;
}

// Payload per creazione prestazione
export interface ServiceCreatePayload {
  doctorId: number;
  patientId: number;
  paymentType: PaymentTypeEnum;
  description: string;
  amountCents: number;
  performedAt: string;
}

// Payload per modifica prestazione
export interface ServiceEditPayload {
  amountCents: number;
  patientName: string | null;
  patientEmail: string | null;
  notes: string | null;
}
