import { Injectable } from '@angular/core';
import { Observable, forkJoin, of, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/auth/auth.service';

// ============================================================================
// INTERFACCE DTO - Allineate ai backend Java records
// ============================================================================

// === Pagination ===
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// === Scheduling (Slots & Appointments) ===
export type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'BLOCKED';
export type VisitMode = 'IN_PERSON' | 'TELEVISIT';
export type AppointmentStatus = 'BOOKED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

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

// === Prescriptions ===
export type PrescriptionStatus = 'DRAFT' | 'ISSUED' | 'EXPIRED' | 'CANCELLED';

export interface PrescriptionItemDto {
  id?: number;
  medicationCode?: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays: number;
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

// === Televisits ===
export type TelevisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

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
}

// === Admissions ===
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

// === Directory (Patients & Doctors) ===
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

// === Consents ===
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

// === Notifications ===
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

// ============================================================================
// SERVICE
// ============================================================================

@Injectable({
  providedIn: 'root'
})
export class DoctorApiService {

  constructor(
    private api: ApiService,
    private auth: AuthService
  ) {}

  // ---------------------------------------------------------------------------
  // HELPERS
  // ---------------------------------------------------------------------------

  getDoctorId(): number | null {
    const claim = this.auth.getAccessTokenClaim('did');
    if (typeof claim === 'number') return claim;
    if (typeof claim === 'string') {
      const parsed = Number(claim);
      return Number.isNaN(parsed) ? null : parsed;
    }
    return null;
  }

  getDepartmentCode(): string | null {
    const claim = this.auth.getAccessTokenClaim('dept');
    return typeof claim === 'string' ? claim : null;
  }

  // ---------------------------------------------------------------------------
  // SLOTS
  // ---------------------------------------------------------------------------

  searchSlots(params: {
    doctorId?: number;
    department?: string;
    mode?: VisitMode;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
  }): Observable<Page<SlotDto>> {
    return this.api.get<Page<SlotDto>>('/api/slots', {
      doctorId: params.doctorId,
      department: params.department,
      mode: params.mode,
      from: params.from,
      to: params.to,
      page: params.page ?? 0,
      size: params.size ?? 100
    });
  }

  createSlot(dto: SlotCreateDto): Observable<SlotDto> {
    return this.api.post<SlotDto>('/api/slots', dto);
  }

  // ---------------------------------------------------------------------------
  // APPOINTMENTS
  // ---------------------------------------------------------------------------

  searchAppointments(params: {
    patientId?: number;
    doctorId?: number;
    department?: string;
    page?: number;
    size?: number;
  }): Observable<Page<AppointmentDto>> {
    return this.api.get<Page<AppointmentDto>>('/api/appointments', {
      patientId: params.patientId,
      doctorId: params.doctorId,
      department: params.department,
      page: params.page ?? 0,
      size: params.size ?? 100
    });
  }

  cancelAppointment(id: number): Observable<void> {
    return this.api.delete<void>(`/api/appointments/${id}`);
  }

  // ---------------------------------------------------------------------------
  // PRESCRIPTIONS
  // ---------------------------------------------------------------------------

  listPrescriptions(params: {
    patientId: number;
    departmentCode: string;
    page?: number;
    size?: number;
  }): Observable<Page<PrescriptionDto>> {
    return this.api.get<Page<PrescriptionDto>>('/api/doctor-prescriptions', {
      patientId: params.patientId,
      departmentCode: params.departmentCode,
      page: params.page ?? 0,
      size: params.size ?? 20
    });
  }

  getPrescription(id: number): Observable<PrescriptionDto> {
    return this.api.get<PrescriptionDto>(`/api/doctor-prescriptions/${id}`);
  }

  createPrescription(dto: PrescriptionCreateDto): Observable<PrescriptionDto> {
    return this.api.post<PrescriptionDto>('/api/doctor-prescriptions', dto);
  }

  cancelPrescription(id: number): Observable<void> {
    return this.api.post<void>(`/api/doctor-prescriptions/${id}/cancel`, {});
  }

  // ---------------------------------------------------------------------------
  // TELEVISITS
  // ---------------------------------------------------------------------------

  searchTelevisits(params: {
    department?: string;
    status?: TelevisitStatus;
    doctorSubject?: string;
    patientSubject?: string;
    page?: number;
    size?: number;
  }): Observable<Page<TelevisitDto>> {
    return this.api.get<Page<TelevisitDto>>('/api/televisits', {
      department: params.department,
      status: params.status,
      doctorSubject: params.doctorSubject,
      patientSubject: params.patientSubject,
      page: params.page ?? 0,
      size: params.size ?? 100
    });
  }

  getTelevisit(id: number): Observable<TelevisitDto> {
    return this.api.get<TelevisitDto>(`/api/televisits/${id}`);
  }

  getDoctorTelevisitToken(id: number): Observable<LiveKitTokenDto> {
    return this.api.post<LiveKitTokenDto>(`/api/televisits/${id}/token/doctor`, {});
  }

  startTelevisit(id: number): Observable<TelevisitDto> {
    return this.api.post<TelevisitDto>(`/api/televisits/${id}/start`, {});
  }

  endTelevisit(id: number): Observable<TelevisitDto> {
    return this.api.post<TelevisitDto>(`/api/televisits/${id}/end`, {});
  }

  cancelTelevisit(id: number): Observable<TelevisitDto> {
    return this.api.post<TelevisitDto>(`/api/televisits/${id}/cancel`, {});
  }

  // ---------------------------------------------------------------------------
  // ADMISSIONS
  // ---------------------------------------------------------------------------

  listAdmissions(params: {
    department?: string;
    status?: AdmissionStatus;
    page?: number;
    size?: number;
  }): Observable<Page<AdmissionDto>> {
    return this.api.get<Page<AdmissionDto>>('/api/admissions', {
      department: params.department,
      status: params.status,
      page: params.page ?? 0,
      size: params.size ?? 100
    });
  }

  createAdmission(dto: AdmissionCreateDto): Observable<AdmissionDto> {
    return this.api.post<AdmissionDto>('/api/admissions', dto);
  }

  dischargeAdmission(id: number): Observable<AdmissionDto> {
    return this.api.post<AdmissionDto>(`/api/admissions/${id}/discharge`, {});
  }

  // ---------------------------------------------------------------------------
  // PATIENTS
  // ---------------------------------------------------------------------------

  searchPatients(params: {
    q?: string;
    department?: string;
    page?: number;
    size?: number;
  }): Observable<Page<PatientDto>> {
    return this.api.get<Page<PatientDto>>('/api/patients', {
      q: params.q,
      department: params.department,
      page: params.page ?? 0,
      size: params.size ?? 20
    });
  }

  getPatient(id: number): Observable<PatientDto> {
    return this.api.get<PatientDto>(`/api/patients/${id}`);
  }

  // ---------------------------------------------------------------------------
  // DOCTORS
  // ---------------------------------------------------------------------------

  searchDoctors(params: {
    q?: string;
    department?: string;
    specialization?: string;
    page?: number;
    size?: number;
  }): Observable<Page<DoctorDto>> {
    return this.api.get<Page<DoctorDto>>('/api/doctors', {
      q: params.q,
      department: params.department,
      specialization: params.specialization,
      page: params.page ?? 0,
      size: params.size ?? 20
    });
  }

  getDoctor(id: number): Observable<DoctorDto> {
    return this.api.get<DoctorDto>(`/api/doctors/${id}`);
  }

  getCurrentDoctor(): Observable<DoctorDto | null> {
    const doctorId = this.getDoctorId();
    if (!doctorId) return of(null);
    return this.getDoctor(doctorId).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Aggiorna il numero di telefono del medico autenticato.
   * L'email non può essere modificata.
   */
  updateMyPhone(phone: string | null): Observable<DoctorDto> {
    return this.api.patch<DoctorDto>('/api/doctor/me', { phone });
  }

  // ---------------------------------------------------------------------------
  // CONSENTS
  // ---------------------------------------------------------------------------

  checkConsent(patientId: number, scope: ConsentScope): Observable<ConsentCheckResponse> {
    const doctorId = this.getDoctorId();
    return this.api.get<ConsentCheckResponse>('/api/consents/check', {
      patientId,
      ...(doctorId && { doctorId }),
      scope
    });
  }

  checkMultipleConsents(patientId: number, scopes: ConsentScope[]): Observable<Map<ConsentScope, boolean>> {
    const checks = scopes.map(scope =>
      this.checkConsent(patientId, scope).pipe(
        map(response => ({ scope, allowed: response.allowed })),
        catchError(() => of({ scope, allowed: false }))
      )
    );
    return forkJoin(checks).pipe(
      map(results => {
        const map = new Map<ConsentScope, boolean>();
        results.forEach(r => map.set(r.scope, r.allowed));
        return map;
      })
    );
  }

  /**
   * Restituisce gli ID dei pazienti che hanno concesso consenso attivo al medico per lo scope specificato.
   * Se doctorId non è fornito, viene usato l'ID del medico autenticato.
   */
  getPatientsWithConsent(scope: ConsentScope, doctorId?: number): Observable<number[]> {
    const resolvedDoctorId = doctorId ?? this.getDoctorId();
    if (!resolvedDoctorId) {
      return of([]);
    }
    return this.api.get<number[]>('/api/consents/patients-with-consent', {
      scope,
      doctorId: resolvedDoctorId
    }).pipe(
      catchError(() => of([]))
    );
  }

  // ---------------------------------------------------------------------------
  // NOTIFICATIONS
  // ---------------------------------------------------------------------------

  listNotifications(params: {
    page?: number;
    size?: number;
  }): Observable<Page<NotificationDto>> {
    return this.api.get<Page<NotificationDto>>('/api/notifications', {
      page: params.page ?? 0,
      size: params.size ?? 50
    });
  }

  // ---------------------------------------------------------------------------
  // DEPARTMENTS
  // ---------------------------------------------------------------------------

  listDepartments(): Observable<DepartmentDto[]> {
    return this.api.get<DepartmentDto[]>('/api/departments').pipe(
      catchError(() => of([]))
    );
  }

  // ---------------------------------------------------------------------------
  // DOCUMENTS
  // ---------------------------------------------------------------------------

  listDocuments(params: {
    patientId?: number;
    page?: number;
    size?: number;
  }): Observable<Page<DocumentDto>> {
    return this.api.get<Page<DocumentDto>>('/api/docs', {
      patientId: params.patientId,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: 'createdAt,desc'
    });
  }

  downloadDocumentUrl(documentId: string): string {
    return `/api/docs/${documentId}/download`;
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.api.delete<void>(`/api/docs/${documentId}`);
  }
}

// DTO per documenti (da aggiungere)
export interface DocumentDto {
  id: string;
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
