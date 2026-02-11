import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, of, forkJoin, map, catchError } from 'rxjs';
import { environment } from '@env/environment';

import {
  UserStatus,
  PrescriptionStatus,
  AdmissionType,
  AdmissionStatus,
  PaymentMethod,
  PaymentStatus,
  RecipientType,
  NotificationChannel,
  NotificationStatus,
  TelevisitStatus,
  ConsentScope,
  ConsentStatus,
  PatientDto,
  PatientPhoneUpdateDto,
  NotificationPreferenceDto,
  PrescriptionItemDto,
  PrescriptionDto,
  AdmissionDto,
  PaymentOrderDto,
  PaymentCreateDto,
  NotificationDto,
  TelevisitDto,
  LiveKitTokenDto,
  DocumentDto,
  ConsentDto,
  ConsentCreateDto,
  PrescriptionWithDetails,
  AdmissionWithDetails,
  ConsentWithDetails,
  DoctorDto,
  DepartmentDto,
  PagedResponse
} from '../dtos/patient-shared.dto';

// Re-export per backward compatibility
export {
  UserStatus,
  PrescriptionStatus,
  AdmissionType,
  AdmissionStatus,
  PaymentMethod,
  PaymentStatus,
  RecipientType,
  NotificationChannel,
  NotificationStatus,
  TelevisitStatus,
  ConsentScope,
  ConsentStatus,
  PatientDto,
  PatientPhoneUpdateDto,
  NotificationPreferenceDto,
  PrescriptionItemDto,
  PrescriptionDto,
  AdmissionDto,
  PaymentOrderDto,
  PaymentCreateDto,
  NotificationDto,
  TelevisitDto,
  LiveKitTokenDto,
  DocumentDto,
  ConsentDto,
  ConsentCreateDto,
  PrescriptionWithDetails,
  AdmissionWithDetails,
  ConsentWithDetails,
  DoctorDto,
  DepartmentDto,
  PagedResponse
} from '../dtos/patient-shared.dto';

@Injectable({
  providedIn: 'root'
})
export class PatientService {

  private readonly gatewayUrl = environment.gatewayUrl;

  constructor(private http: HttpClient) {}

  // ============ PROFILO PAZIENTE ============

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

  // ============ PREFERENZE NOTIFICHE ============

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

  // ============ PRESCRIZIONI ============

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

  // ============ RICOVERI ============

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
    );
  }

  // ============ PAGAMENTI ============

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

  /**
   * Cattura (paga) un pagamento esistente
   */
  capturePayment(paymentId: number): Observable<PaymentOrderDto> {
    return this.http.post<PaymentOrderDto>(`${this.gatewayUrl}/api/payments/${paymentId}/capture`, {});
  }

  /**
   * Scarica la ricevuta di un pagamento in PDF
   */
  downloadReceipt(paymentId: number): Observable<Blob> {
    return this.http.get(`${this.gatewayUrl}/api/payments/${paymentId}/receipt`, {
      responseType: 'blob'
    });
  }

  // ============ NOTIFICHE ============

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
    );
  }

  /**
   * Marca una notifica come letta
   */
  markNotificationAsRead(id: number): Observable<NotificationDto> {
    return this.http.patch<NotificationDto>(`${this.gatewayUrl}/api/notifications/${id}/read`, {});
  }

  /**
   * Archivia una notifica
   */
  archiveNotification(id: number): Observable<NotificationDto> {
    return this.http.patch<NotificationDto>(`${this.gatewayUrl}/api/notifications/${id}/archive`, {});
  }

  /**
   * Marca tutte le notifiche non lette come lette
   */
  markAllNotificationsAsRead(): Observable<{ updated: number }> {
    return this.http.post<{ updated: number }>(`${this.gatewayUrl}/api/notifications/read-all`, {});
  }

  // ============ TELEVISITE ============

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

  // ============ DOCUMENTI ============

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

  // ============ CONSENSI ============

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

  // ============ MEDICI (helper) ============

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

  // ============ HELPER DI ARRICCHIMENTO ============

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
      doctors: this.getDoctors({ size: 1000 }).pipe(map(r => r.content)),
      departments: this.getDepartments()
    });
  }
}
