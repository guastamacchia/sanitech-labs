// ============================================================
// Shared DTOs per Patient area
// Estratti da patient.service.ts e scheduling.service.ts
// ============================================================

// ============ ENUM E TIPI (da patient.service) ============

export type UserStatus = 'PENDING' | 'ACTIVE' | 'DISABLED';

// Prescrizione
export type PrescriptionStatus = 'ISSUED' | 'DISPENSED' | 'CANCELLED';

// Ricovero
export type AdmissionType = 'ORDINARY' | 'DAY_HOSPITAL' | 'EMERGENCY';
export type AdmissionStatus = 'ADMITTED' | 'DISCHARGED' | 'CANCELLED';

// Pagamento
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'MANUAL';
export type PaymentStatus = 'PENDING' | 'AUTHORIZED' | 'CAPTURED' | 'FAILED' | 'REFUNDED';

// Notifica
export type RecipientType = 'PATIENT' | 'DOCTOR' | 'ADMIN';
export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'BOUNCED';

// Televisita
export type TelevisitStatus = 'CREATED' | 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELED';

// Consenso
export type ConsentScope = 'MEDICAL_RECORDS' | 'PRESCRIPTIONS' | 'TELEVISION';
export type ConsentStatus = 'GRANTED' | 'REVOKED' | 'EXPIRED';

// ============ ENUM E TIPI (da scheduling.service) ============

export type VisitMode = 'IN_PERSON' | 'TELEVISIT';
export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'CANCELLED';
export type AppointmentStatus = 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

// ============ DTOs condivisi (da scheduling.service) ============

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// DTO dal backend svc-directory
export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentCode: string;
  facilityCode: string;
}

export interface DepartmentDto {
  id: number;
  code: string;
  name: string;
  facilityCode: string;
}

export interface FacilityDto {
  id: number;
  code: string;
  name: string;
}

// DTO dal backend svc-scheduling
export interface SlotDto {
  id: number;
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string; // ISO instant
  endAt: string;
  status: SlotStatus;
}

export interface AppointmentDto {
  id: number;
  slotId: number;
  patientId: number;
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string;
  endAt: string;
  status: AppointmentStatus;
  reason?: string;
}

export interface AppointmentCreateDto {
  slotId: number;
  patientId?: number;
  reason?: string;
}

// ============ Extended types per il frontend (da scheduling.service) ============

export interface DoctorWithDetails extends DoctorDto {
  departmentName?: string;
  facilityName?: string;
}

export interface AppointmentWithDetails extends AppointmentDto {
  doctorName?: string;
  departmentName?: string;
  facilityName?: string;
}

// ============ DTOs (da patient.service) ============

export interface PatientDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  fiscalCode: string;
  birthDate: string; // LocalDate -> string
  address?: string;
  status: UserStatus;
  registeredAt: string;
  activatedAt?: string;
  departments: DepartmentDto[];
}

export interface PatientPhoneUpdateDto {
  phone: string | null;
}

export interface NotificationPreferenceDto {
  id?: number;
  emailReminders: boolean;
  smsReminders: boolean;
  emailDocuments: boolean;
  smsDocuments: boolean;
  emailPayments: boolean;
  smsPayments: boolean;
}

export interface PrescriptionItemDto {
  id: number;
  medicationCode: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays?: number;
  instructions?: string;
  sortOrder?: number;
}

export interface PrescriptionDto {
  id: number;
  patientId: number;
  doctorId: number;
  departmentCode: string;
  status: PrescriptionStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  issuedAt?: string;
  cancelledAt?: string;
  items: PrescriptionItemDto[];
}

export interface AdmissionDto {
  id: number;
  patientId: number;
  departmentCode: string;
  admissionType: AdmissionType;
  status: AdmissionStatus;
  admittedAt: string;
  dischargedAt?: string;
  notes?: string;
  attendingDoctorId?: number;
}

export interface PaymentOrderDto {
  id: number;
  appointmentId?: number;
  patientId: number;
  patientEmail: string;
  patientName: string;
  amountCents: number;
  currency: string;
  method?: PaymentMethod;
  provider?: string;
  providerReference?: string;
  status: PaymentStatus;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentCreateDto {
  appointmentId?: number;
  amountCents: number;
  currency: string;
  method: PaymentMethod;
  description?: string;
}

export interface NotificationDto {
  id: number;
  recipientType: RecipientType;
  recipientId: string;
  channel: NotificationChannel;
  toAddress: string;
  subject: string;
  body: string;
  status: NotificationStatus;
  createdAt: string;
  sentAt?: string;
  errorMessage?: string;
}

export interface TelevisitDto {
  id: number;
  roomName: string;
  department: string;
  doctorSubject: string;
  patientSubject: string;
  scheduledAt: string;
  status: TelevisitStatus;
}

export interface LiveKitTokenDto {
  token: string;
  url: string;
  roomName: string;
  participantId: string;
}

export interface DocumentDto {
  id: string; // UUID
  patientId: number;
  departmentCode: string;
  documentType: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256?: string;
  description?: string;
  createdAt: string;
}

export interface ConsentDto {
  id: number;
  patientId: number;
  doctorId: number;
  scope: ConsentScope;
  status: ConsentStatus;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
  updatedAt: string;
}

export interface ConsentCreateDto {
  doctorId: number;
  scope: ConsentScope;
  expiresAt?: string;
}

// ============ Extended types per il frontend (da patient.service) ============

export interface PrescriptionWithDetails extends PrescriptionDto {
  doctorName?: string;
  departmentName?: string;
}

export interface AdmissionWithDetails extends AdmissionDto {
  departmentName?: string;
  attendingDoctorName?: string;
}

export interface ConsentWithDetails extends ConsentDto {
  doctorName?: string;
  doctorSpecialization?: string;
}
