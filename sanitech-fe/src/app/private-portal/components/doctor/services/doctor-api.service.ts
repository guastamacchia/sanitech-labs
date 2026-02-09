import { Injectable } from '@angular/core';
import { Observable, forkJoin, of, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '@core/services/api.service';
import { AuthService } from '@core/auth/auth.service';

// ============================================================================
// RE-EXPORT DTOs per backward compatibility
// Le definizioni sono ora centralizzate in ../dtos/doctor-shared.dto.ts
// ============================================================================
export {
  Page,
  SlotStatus,
  VisitMode,
  AppointmentStatus,
  SlotDto,
  SlotCreateDto,
  AppointmentDto,
  PrescriptionStatus,
  PrescriptionItemDto,
  PrescriptionDto,
  PrescriptionCreateDto,
  TelevisitStatus,
  TelevisitDto,
  LiveKitTokenDto,
  AdmissionStatus,
  AdmissionType,
  AdmissionDto,
  AdmissionCreateDto,
  AdmissionUpdateDto,
  UserStatus,
  DepartmentDto,
  PatientDto,
  DoctorDto,
  ConsentScope,
  ConsentStatus,
  ConsentCheckResponse,
  NotificationChannel,
  NotificationStatus,
  RecipientType,
  NotificationDto,
  DocumentDto
} from '../dtos/doctor-shared.dto';

import type {
  Page,
  VisitMode,
  SlotDto,
  SlotCreateDto,
  AppointmentDto,
  PrescriptionDto,
  PrescriptionCreateDto,
  TelevisitStatus,
  TelevisitDto,
  LiveKitTokenDto,
  AdmissionStatus,
  AdmissionDto,
  AdmissionCreateDto,
  AdmissionUpdateDto,
  PatientDto,
  DoctorDto,
  ConsentScope,
  ConsentCheckResponse,
  NotificationDto,
  DepartmentDto,
  DocumentDto
} from '../dtos/doctor-shared.dto';

// ============================================================================
// SERVIZIO
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
  // HELPER
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
    if (typeof claim === 'string') return claim;
    if (Array.isArray(claim) && claim.length > 0) return String(claim[0]);
    return null;
  }

  /**
   * Restituisce il preferred_username del JWT del dottore autenticato.
   * Utilizzato per verificare l'ownership dei documenti (il backend salva jwt.getName() = preferred_username).
   */
  getDoctorSubject(): string | null {
    const claim = this.auth.getAccessTokenClaim('preferred_username');
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

  // BUG-003: Aggiunto metodo per cancellare uno slot (medico proprietario)
  cancelSlot(id: number): Observable<void> {
    return this.api.delete<void>(`/api/slots/${id}`);
  }

  // ---------------------------------------------------------------------------
  // APPUNTAMENTI
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

  // BUG-012: Aggiunto metodo per completare un appuntamento
  completeAppointment(id: number): Observable<AppointmentDto> {
    return this.api.post<AppointmentDto>(`/api/appointments/${id}/complete`, {});
  }

  // ---------------------------------------------------------------------------
  // PRESCRIZIONI
  // ---------------------------------------------------------------------------

  listPrescriptions(params: {
    patientId: number;
    departmentCode: string;
    page?: number;
    size?: number;
  }): Observable<Page<PrescriptionDto>> {
    return this.api.get<Page<PrescriptionDto>>('/api/doctor/prescriptions', {
      patientId: params.patientId,
      departmentCode: params.departmentCode,
      page: params.page ?? 0,
      size: params.size ?? 20
    });
  }

  getPrescription(id: number): Observable<PrescriptionDto> {
    return this.api.get<PrescriptionDto>(`/api/doctor/prescriptions/${id}`);
  }

  createPrescription(dto: PrescriptionCreateDto): Observable<PrescriptionDto> {
    return this.api.post<PrescriptionDto>('/api/doctor/prescriptions', dto);
  }

  cancelPrescription(id: number): Observable<void> {
    return this.api.post<void>(`/api/doctor/prescriptions/${id}/cancel`, {});
  }

  // ---------------------------------------------------------------------------
  // TELEVISITE
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

  updateTelevisitNotes(id: number, notes: string): Observable<TelevisitDto> {
    return this.api.patch<TelevisitDto>(`/api/televisits/${id}/notes`, { notes });
  }

  // ---------------------------------------------------------------------------
  // RICOVERI
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

  // BUG-002/003: Aggiornamento parziale ricovero (attendingDoctorId, notes)
  updateAdmission(id: number, dto: AdmissionUpdateDto): Observable<AdmissionDto> {
    return this.api.patch<AdmissionDto>(`/api/admissions/${id}`, dto);
  }

  // ---------------------------------------------------------------------------
  // PAZIENTI
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
  // MEDICI
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
  // CONSENSI
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
  // NOTIFICHE
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

  markNotificationAsRead(id: number): Observable<NotificationDto> {
    return this.api.patch<NotificationDto>(`/api/notifications/${id}/read`);
  }

  archiveNotification(id: number): Observable<NotificationDto> {
    return this.api.patch<NotificationDto>(`/api/notifications/${id}/archive`);
  }

  markAllNotificationsAsRead(): Observable<{ updated: number }> {
    return this.api.post<{ updated: number }>('/api/notifications/read-all');
  }

  // ---------------------------------------------------------------------------
  // REPARTI
  // ---------------------------------------------------------------------------

  listDepartments(): Observable<DepartmentDto[]> {
    return this.api.get<DepartmentDto[]>('/api/departments').pipe(
      catchError(() => of([]))
    );
  }

  // ---------------------------------------------------------------------------
  // DOCUMENTI
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

  uploadDocument(params: {
    file: File;
    patientId: number;
    departmentCode: string;
    documentType: string;
    description?: string;
  }): Observable<DocumentDto> {
    const formData = new FormData();
    formData.append('file', params.file, params.file.name);
    formData.append('patientId', String(params.patientId));
    formData.append('departmentCode', params.departmentCode);
    formData.append('documentType', params.documentType);
    if (params.description) {
      formData.append('description', params.description);
    }
    return this.api.postMultipart<DocumentDto>('/api/docs/upload', formData);
  }

  downloadDocumentUrl(documentId: string): string {
    return `/api/docs/${documentId}/download`;
  }

  downloadDocumentBlob(documentId: string): Observable<Blob> {
    return this.api.getBlob(`/api/docs/${documentId}/download`);
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.api.delete<void>(`/api/docs/${documentId}`);
  }
}
