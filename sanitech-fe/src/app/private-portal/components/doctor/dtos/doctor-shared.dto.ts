// ============================================================================
// INTERFACCE DTO CONDIVISE - Allineate ai backend Java records
// Estratte da DoctorApiService e DoctorDocsService per evitare duplicazione
// ============================================================================

// === Paginazione ===
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// === Pianificazione (Slot e Appuntamenti) ===
// BUG-015: Allineato con backend (AVAILABLE, BOOKED, CANCELLED)
export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'CANCELLED';
export type VisitMode = 'IN_PERSON' | 'TELEVISIT';
// BUG-015: Allineato con backend (BOOKED, COMPLETED, CANCELLED)
export type AppointmentStatus = 'BOOKED' | 'CANCELLED' | 'COMPLETED';

export interface SlotDto {
  id: number;
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string;
  endAt: string;
  status: SlotStatus;
}

export interface SlotCreateDto {
  doctorId: number;
  departmentCode: string;
  mode: VisitMode;
  startAt: string;
  endAt: string;
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

// === Prescrizioni ===
export type PrescriptionStatus = 'DRAFT' | 'ISSUED' | 'CANCELLED';

export interface PrescriptionItemDto {
  id?: number;
  medicationCode?: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays?: number | null;
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
  updatedAt?: string;
  issuedAt?: string;
  cancelledAt?: string;
  items: PrescriptionItemDto[];
}

export interface PrescriptionCreateDto {
  patientId: number;
  departmentCode: string;
  notes?: string;
  items: Omit<PrescriptionItemDto, 'id'>[];
}

// === Televisite ===
// Stati allineati al backend Java enum: CREATED, SCHEDULED, ACTIVE, ENDED, CANCELED
export type TelevisitStatus = 'CREATED' | 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELED';

export interface TelevisitDto {
  id: number;
  roomName: string;
  department: string;
  doctorSubject: string;
  patientSubject: string;
  scheduledAt: string;
  status: TelevisitStatus;
  notes?: string;
  patientName?: string;
  doctorName?: string;
}

export interface LiveKitTokenDto {
  token: string;
}

// === Ricoveri ===
export type AdmissionStatus = 'ACTIVE' | 'DISCHARGED' | 'TRANSFERRED';
export type AdmissionType = 'EMERGENCY' | 'SCHEDULED' | 'TRANSFER';

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

export interface AdmissionCreateDto {
  patientId: number;
  departmentCode: string;
  admissionType: AdmissionType;
  notes?: string;
}

// === Anagrafica (Pazienti e Medici) ===
export type UserStatus = 'PENDING' | 'ACTIVE' | 'DISABLED';

export interface DepartmentDto {
  id?: number;
  code: string;
  name: string;
  facilityId?: number;
}

export interface PatientDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  fiscalCode?: string;
  birthDate?: string;
  address?: string;
  status?: UserStatus;
  registeredAt?: string;
  activatedAt?: string;
  departments?: DepartmentDto[];
}

export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialization?: string;
  status?: UserStatus;
  createdAt?: string;
  activatedAt?: string;
  departmentCode?: string;
  departmentName?: string;
  facilityCode?: string;
  facilityName?: string;
}

// === Consensi ===
export type ConsentScope = 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT';
export type ConsentStatus = 'GRANTED' | 'REVOKED';

export interface ConsentCheckResponse {
  patientId: number;
  doctorId: number;
  scope: ConsentScope;
  allowed: boolean;
  status: ConsentStatus;
  expiresAt?: string;
}

// === Notifiche ===
export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'READ';
export type RecipientType = 'PATIENT' | 'DOCTOR' | 'ADMIN';

export interface NotificationDto {
  id: number;
  recipientType: RecipientType;
  recipientId: string;
  channel: NotificationChannel;
  toAddress?: string;
  subject: string;
  body: string;
  status: NotificationStatus;
  createdAt: string;
  sentAt?: string;
  errorMessage?: string;
}

// === Documenti ===
export interface DocumentDto {
  id: string;
  patientId: number;
  uploadedBy: string;
  departmentCode: string;
  documentType: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  checksumSha256?: string;
  description?: string;
  createdAt: string;
}

// === Errori (usati da DoctorDocsService) ===
export interface ProblemDetails {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
}
