import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of, forkJoin, map, catchError } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { DoctorDto, DepartmentDto, PagedResponse } from './scheduling.service';

// ============ ENUMS & TYPES ============

export type UserStatus = 'PENDING' | 'ACTIVE' | 'DISABLED';

// Prescription
export type PrescriptionStatus = 'ISSUED' | 'DISPENSED' | 'CANCELLED';

// Admission
export type AdmissionType = 'ORDINARY' | 'DAY_HOSPITAL' | 'EMERGENCY';
export type AdmissionStatus = 'ADMITTED' | 'DISCHARGED' | 'CANCELLED';

// Payment
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'MANUAL';
export type PaymentStatus = 'PENDING' | 'AUTHORIZED' | 'CAPTURED' | 'FAILED' | 'REFUNDED';

// Notification
export type RecipientType = 'PATIENT' | 'DOCTOR' | 'ADMIN';
export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'BOUNCED';

// Televisit
export type TelevisitStatus = 'CREATED' | 'SCHEDULED' | 'ACTIVE' | 'ENDED' | 'CANCELED';

// Consent
export type ConsentScope = 'MEDICAL_RECORDS' | 'PRESCRIPTIONS' | 'TELEVISION';
export type ConsentStatus = 'GRANTED' | 'REVOKED' | 'EXPIRED';

// ============ DTOs ============

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
  phone: string;
}

export interface NotificationPreferenceDto {
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

// ============ Extended types per il frontend ============

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

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private readonly gatewayUrl = environment.gatewayUrl;

  constructor(private http: HttpClient) {}

  // ============ PATIENT PROFILE ============

  /**
   * Recupera il profilo del paziente autenticato
   */
  getMyProfile(): Observable<PatientDto> {
    return this.http.get<PatientDto>(`${this.gatewayUrl}/api/patient/me`);
  }

  /**
   * Aggiorna il numero di telefono del paziente
   */
  updateMyPhone(dto: PatientPhoneUpdateDto): Observable<PatientDto> {
    return this.http.patch<PatientDto>(`${this.gatewayUrl}/api/patient/me`, dto);
  }

  // ============ NOTIFICATION PREFERENCES ============

  /**
   * Recupera le preferenze di notifica del paziente autenticato
   */
  getNotificationPreferences(): Observable<NotificationPreferenceDto> {
    return this.http.get<NotificationPreferenceDto>(`${this.gatewayUrl}/api/patient/me/preferences`);
  }

  /**
   * Aggiorna le preferenze di notifica del paziente autenticato
   */
  updateNotificationPreferences(dto: NotificationPreferenceDto): Observable<NotificationPreferenceDto> {
    return this.http.put<NotificationPreferenceDto>(`${this.gatewayUrl}/api/patient/me/preferences`, dto);
  }

  // ============ PRESCRIPTIONS ============

  /**
   * Recupera le prescrizioni del paziente
   */
  getPrescriptions(params?: {
    status?: PrescriptionStatus;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PagedResponse<PrescriptionDto>> {
    let httpParams = new HttpParams();
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<PagedResponse<PrescriptionDto>>(
      `${this.gatewayUrl}/api/prescriptions`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  /**
   * Recupera una singola prescrizione
   */
  getPrescription(id: number): Observable<PrescriptionDto | null> {
    return this.http.get<PrescriptionDto>(`${this.gatewayUrl}/api/prescriptions/${id}`).pipe(
      catchError(() => of(null))
    );
  }

  // ============ ADMISSIONS ============

  /**
   * Recupera i ricoveri del paziente
   */
  getMyAdmissions(params?: {
    status?: AdmissionStatus;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PagedResponse<AdmissionDto>> {
    let httpParams = new HttpParams();
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<PagedResponse<AdmissionDto>>(
      `${this.gatewayUrl}/api/admissions/me`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  // ============ PAYMENTS ============

  /**
   * Recupera i pagamenti del paziente
   */
  getPayments(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PagedResponse<PaymentOrderDto>> {
    let httpParams = new HttpParams();
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<PagedResponse<PaymentOrderDto>>(
      `${this.gatewayUrl}/api/payments`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  /**
   * Recupera un singolo pagamento
   */
  getPayment(id: number): Observable<PaymentOrderDto | null> {
    return this.http.get<PaymentOrderDto>(`${this.gatewayUrl}/api/payments/${id}`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Crea un nuovo pagamento (con idempotency key)
   */
  createPayment(dto: PaymentCreateDto, idempotencyKey: string): Observable<PaymentOrderDto> {
    const headers = new HttpHeaders().set('X-Idempotency-Key', idempotencyKey);
    return this.http.post<PaymentOrderDto>(`${this.gatewayUrl}/api/payments`, dto, { headers });
  }

  // ============ NOTIFICATIONS ============

  /**
   * Recupera le notifiche del paziente
   */
  getNotifications(params?: {
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PagedResponse<NotificationDto>> {
    let httpParams = new HttpParams();
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<PagedResponse<NotificationDto>>(
      `${this.gatewayUrl}/api/notifications`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  // ============ TELEVISITS ============

  /**
   * Recupera le televisite del paziente autenticato.
   * Usa l'endpoint patient-specific che filtra automaticamente per patientSubject.
   */
  getTelevisits(params?: {
    department?: string;
    status?: TelevisitStatus;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<PagedResponse<TelevisitDto>> {
    let httpParams = new HttpParams();
    if (params?.department) httpParams = httpParams.set('department', params.department);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<PagedResponse<TelevisitDto>>(
      `${this.gatewayUrl}/api/patient/televisits`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  /**
   * Recupera una singola televisita del paziente autenticato.
   */
  getTelevisit(id: number): Observable<TelevisitDto | null> {
    return this.http.get<TelevisitDto>(`${this.gatewayUrl}/api/patient/televisits/${id}`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Genera token LiveKit per il paziente autenticato.
   * Usa l'endpoint patient-specific che verifica l'ownership della televisita.
   */
  getPatientTelevisitToken(televisitId: number): Observable<LiveKitTokenDto> {
    return this.http.post<LiveKitTokenDto>(
      `${this.gatewayUrl}/api/patient/televisits/${televisitId}/token`,
      {}
    );
  }

  // ============ DOCUMENTS ============

  /**
   * Recupera i documenti del paziente
   */
  getDocuments(params?: {
    departmentCode?: string;
    documentType?: string;
    page?: number;
    size?: number;
  }): Observable<PagedResponse<DocumentDto>> {
    let httpParams = new HttpParams();
    if (params?.departmentCode) httpParams = httpParams.set('departmentCode', params.departmentCode);
    if (params?.documentType) httpParams = httpParams.set('documentType', params.documentType);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PagedResponse<DocumentDto>>(
      `${this.gatewayUrl}/api/docs`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  /**
   * Carica un nuovo documento
   */
  uploadDocument(file: File, metadata: {
    departmentCode: string;
    documentType: string;
    description?: string;
  }): Observable<DocumentDto> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('departmentCode', metadata.departmentCode);
    formData.append('documentType', metadata.documentType);
    if (metadata.description) {
      formData.append('description', metadata.description);
    }

    return this.http.post<DocumentDto>(`${this.gatewayUrl}/api/docs/upload`, formData);
  }

  /**
   * Scarica un documento
   */
  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`${this.gatewayUrl}/api/docs/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  /**
   * Elimina un documento
   */
  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/api/docs/${documentId}`);
  }

  // ============ CONSENTS ============

  /**
   * Recupera i consensi concessi ai medici
   */
  getDoctorConsents(): Observable<ConsentDto[]> {
    return this.http.get<ConsentDto[]>(`${this.gatewayUrl}/api/consents/me/doctors`).pipe(
      catchError(() => of([]))
    );
  }

  /**
   * Concede un consenso a un medico
   */
  grantDoctorConsent(dto: ConsentCreateDto): Observable<ConsentDto> {
    return this.http.post<ConsentDto>(`${this.gatewayUrl}/api/consents/me/doctors`, dto);
  }

  /**
   * Revoca un consenso a un medico
   */
  revokeDoctorConsent(doctorId: number, scope: ConsentScope): Observable<void> {
    return this.http.delete<void>(`${this.gatewayUrl}/api/consents/me/doctors/${doctorId}/${scope}`);
  }

  // ============ DOCTORS (helper) ============

  /**
   * Recupera un medico specifico (per arricchire dati)
   */
  getDoctor(id: number): Observable<DoctorDto | null> {
    return this.http.get<DoctorDto>(`${this.gatewayUrl}/api/doctors/${id}`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Recupera i medici (per arricchire dati)
   */
  getDoctors(params?: {
    page?: number;
    size?: number;
  }): Observable<PagedResponse<DoctorDto>> {
    let httpParams = new HttpParams();
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PagedResponse<DoctorDto>>(
      `${this.gatewayUrl}/api/doctors`,
      { params: httpParams }
    ).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }))
    );
  }

  /**
   * Recupera i reparti
   */
  getDepartments(): Observable<DepartmentDto[]> {
    return this.http.get<DepartmentDto[]>(`${this.gatewayUrl}/api/departments`).pipe(
      catchError(() => of([]))
    );
  }

  // ============ ENRICHMENT HELPERS ============

  /**
   * Arricchisce le prescrizioni con i nomi dei medici e reparti
   */
  enrichPrescriptions(
    prescriptions: PrescriptionDto[],
    doctors: DoctorDto[],
    departments: DepartmentDto[]
  ): PrescriptionWithDetails[] {
    const docMap = new Map(doctors.map(d => [d.id, d]));
    const deptMap = new Map(departments.map(d => [d.code, d]));

    return prescriptions.map(p => {
      const doctor = docMap.get(p.doctorId);
      const dept = deptMap.get(p.departmentCode);
      return {
        ...p,
        doctorName: doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : undefined,
        departmentName: dept?.name
      };
    });
  }

  /**
   * Arricchisce i ricoveri con i nomi dei medici e reparti
   */
  enrichAdmissions(
    admissions: AdmissionDto[],
    doctors: DoctorDto[],
    departments: DepartmentDto[]
  ): AdmissionWithDetails[] {
    const docMap = new Map(doctors.map(d => [d.id, d]));
    const deptMap = new Map(departments.map(d => [d.code, d]));

    return admissions.map(a => {
      const doctor = a.attendingDoctorId ? docMap.get(a.attendingDoctorId) : undefined;
      const dept = deptMap.get(a.departmentCode);
      return {
        ...a,
        departmentName: dept?.name,
        attendingDoctorName: doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : undefined
      };
    });
  }

  /**
   * Arricchisce i consensi con i dati dei medici
   */
  enrichConsents(
    consents: ConsentDto[],
    doctors: DoctorDto[],
    departments: DepartmentDto[]
  ): ConsentWithDetails[] {
    const docMap = new Map(doctors.map(d => [d.id, d]));
    const deptMap = new Map(departments.map(d => [d.code, d]));

    return consents.map(c => {
      const doctor = docMap.get(c.doctorId);
      const dept = doctor ? deptMap.get(doctor.departmentCode) : undefined;
      return {
        ...c,
        doctorName: doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : undefined,
        doctorSpecialization: dept?.name || doctor?.departmentCode
      };
    });
  }

  /**
   * Carica tutti i dati per arricchimento (medici e reparti)
   */
  loadEnrichmentData(): Observable<{
    doctors: DoctorDto[];
    departments: DepartmentDto[];
  }> {
    return forkJoin({
      doctors: this.getDoctors({ size: 200 }).pipe(map(r => r.content)),
      departments: this.getDepartments()
    });
  }
}
