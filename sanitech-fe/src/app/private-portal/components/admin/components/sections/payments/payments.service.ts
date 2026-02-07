import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import {
  PaymentItem,
  PaymentPage,
  PaymentStats,
  ServicePerformedItem,
  ServicePerformedStats,
  ServiceCreatePayload,
  ServiceEditPayload,
  DoctorItem,
  DoctorApiItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  PagedResponse
} from './dtos/payments.dto';

@Injectable({ providedIn: 'root' })
export class PaymentsService {

  constructor(private api: ApiService) {}

  // — Caricamento pagamenti —

  loadPayments(): Observable<PaymentItem[]> {
    return this.api.request<PaymentItem[] | PaymentPage>('GET', '/api/payments').pipe(
      map(payments => {
        const rawPayments = Array.isArray(payments) ? payments : (payments?.content ?? []);
        return rawPayments.map((p) => this.mapPaymentFromBackend(p));
      })
    );
  }

  // — Caricamento prestazioni —

  loadServicesPerformed(serviceStatusFilter: string): Observable<ServicePerformedItem[]> {
    const statusParam = serviceStatusFilter !== 'ALL' ? `?status=${serviceStatusFilter}&size=1000` : '?size=1000';
    return this.api.request<{ content: ServicePerformedItem[], totalElements?: number } | ServicePerformedItem[]>(
      'GET', `/api/admin/services${statusParam}`
    ).pipe(
      map(services => {
        const rawServices = Array.isArray(services) ? services : (services?.content ?? []);
        return rawServices.map((s) => this.mapServiceFromBackend(s));
      })
    );
  }

  // — Statistiche prestazioni —

  loadServiceStats(): Observable<ServicePerformedStats> {
    return this.api.request<ServicePerformedStats>('GET', '/api/admin/services/stats');
  }

  // — Caricamento dati di supporto —

  loadDoctors(): Observable<DoctorItem[]> {
    return this.api.request<DoctorItem[] | PagedResponse<DoctorApiItem>>('GET', '/api/doctors').pipe(
      map(data => this.normalizeDoctorList(data))
    );
  }

  loadPatients(): Observable<PatientItem[]> {
    return this.api.request<PatientItem[] | PagedResponse<PatientItem>>('GET', '/api/patients').pipe(
      map(data => this.normalizeList(data))
    );
  }

  loadFacilities(): Observable<FacilityItem[]> {
    return this.api.request<FacilityItem[]>('GET', '/api/facilities');
  }

  loadDepartments(): Observable<DepartmentItem[]> {
    return this.api.request<any[]>('GET', '/api/departments').pipe(
      map(departments => {
        if (!departments?.length) return [];
        return departments.map(dept => ({
          id: dept.id,
          code: dept.code,
          name: dept.name,
          facilityCode: dept.facilityCode ?? dept.facility_code ?? dept.facility?.code ?? undefined
        } as DepartmentItem));
      })
    );
  }

  // — Pazienti con consenso per un medico —

  loadPatientsWithConsent(doctorId: number): Observable<number[]> {
    return this.api.request<number[]>('GET', `/api/consents/patients-with-consent?scope=RECORDS&doctorId=${doctorId}`);
  }

  // — Azioni pagamenti —

  capturePayment(paymentId: number): Observable<PaymentItem> {
    return this.api.request<PaymentItem>('POST', `/api/admin/payments/${paymentId}/capture`).pipe(
      map(updated => this.mapPaymentFromBackend(updated))
    );
  }

  sendPaymentReminder(paymentId: number): Observable<void> {
    return this.api.request<void>('POST', `/api/admin/payments/${paymentId}/reminder`);
  }

  cancelPayment(paymentId: number): Observable<PaymentItem> {
    return this.api.request<PaymentItem>('POST', `/api/admin/payments/${paymentId}/cancel`);
  }

  // — Azioni prestazioni —

  markServiceAsPaid(serviceId: number): Observable<ServicePerformedItem> {
    return this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${serviceId}/paid`).pipe(
      map(updated => this.mapServiceFromBackend(updated))
    );
  }

  markServiceAsFree(serviceId: number, reason?: string): Observable<ServicePerformedItem> {
    const reasonParam = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    return this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${serviceId}/free${reasonParam}`).pipe(
      map(updated => this.mapServiceFromBackend(updated))
    );
  }

  updateService(serviceId: number, payload: ServiceEditPayload): Observable<ServicePerformedItem> {
    return this.api.request<ServicePerformedItem>('PATCH', `/api/admin/services/${serviceId}`, payload).pipe(
      map(updated => this.mapServiceFromBackend(updated))
    );
  }

  deleteService(serviceId: number): Observable<void> {
    return this.api.request<void>('DELETE', `/api/admin/services/${serviceId}`);
  }

  sendServiceReminder(serviceId: number): Observable<ServicePerformedItem> {
    return this.api.request<ServicePerformedItem>('POST', `/api/admin/services/${serviceId}/reminder`).pipe(
      map(updated => this.mapServiceFromBackend(updated))
    );
  }

  sendBulkServiceReminders(ids: number[]): Observable<{ sent: number; skipped: number }> {
    return this.api.request<{ sent: number; skipped: number }>('POST', '/api/admin/services/bulk-reminders', ids);
  }

  // — Creazione prestazione —

  createService(payload: ServiceCreatePayload): Observable<ServicePerformedItem> {
    return this.api.request<ServicePerformedItem>('POST', '/api/admin/services', payload).pipe(
      map(service => this.mapServiceFromBackend(service))
    );
  }

  // — Mapping backend —

  mapPaymentFromBackend(p: PaymentItem): PaymentItem {
    return {
      ...p,
      amount: p.amountCents ? p.amountCents / 100 : p.amount,
      service: p.description || p.service
    };
  }

  mapServiceFromBackend(s: ServicePerformedItem): ServicePerformedItem {
    return {
      ...s,
      amount: s.amountCents ? s.amountCents / 100 : 0
    };
  }

  // — Statistiche pagamenti (calcolo locale) —

  computePaymentStats(payments: PaymentItem[]): PaymentStats {
    const total = payments.length;
    if (total === 0) {
      return { totalPayments: 0, completedWithin7Days: 0, completedWithReminder: 0, stillPending: 0, percentWithin7Days: 0, percentWithReminder: 0, percentPending: 0 };
    }
    const captured = payments.filter((p) => p.status === 'CAPTURED').length;
    const pending = payments.filter((p) => p.status === 'CREATED').length;
    const failed = payments.filter((p) => p.status === 'FAILED').length;
    return {
      totalPayments: total,
      completedWithin7Days: captured,
      completedWithReminder: failed,
      stillPending: pending,
      percentWithin7Days: Math.round((captured / total) * 100),
      percentWithReminder: Math.round((failed / total) * 100),
      percentPending: Math.round((pending / total) * 100)
    };
  }

  // — Normalizzazione —

  private normalizeList<T>(data?: T[] | PagedResponse<T>): T[] {
    if (!data) return [];
    if (Array.isArray(data)) return data;
    return data.content ?? [];
  }

  private normalizeDoctorList(data?: DoctorItem[] | PagedResponse<DoctorApiItem>): DoctorItem[] {
    return this.normalizeList(data).map((doctor) => {
      const apiDoctor = doctor as DoctorApiItem;
      const department = apiDoctor.departmentCode ?? apiDoctor.departments?.[0]?.code ?? '';
      const facility = apiDoctor.facilityCode ?? '';
      return {
        id: apiDoctor.id,
        firstName: apiDoctor.firstName,
        lastName: apiDoctor.lastName,
        departmentCode: department || undefined,
        facilityCode: facility || undefined,
        email: apiDoctor.email,
        phone: apiDoctor.phone
      };
    });
  }

}
