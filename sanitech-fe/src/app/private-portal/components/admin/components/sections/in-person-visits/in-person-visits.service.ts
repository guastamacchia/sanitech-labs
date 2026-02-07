import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import {
  InPersonVisitItem,
  AvailableSlotItem,
  SpringPage,
  DoctorItem,
  DoctorApiItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  PagedResponse,
  VisitCreatePayload
} from './dtos/in-person-visits.dto';

@Injectable({ providedIn: 'root' })
export class InPersonVisitsService {

  constructor(private api: ApiService) {}

  // — Caricamento visite in presenza —

  loadInPersonVisits(): Observable<InPersonVisitItem[]> {
    return this.api.request<SpringPage<InPersonVisitItem>>('GET', '/api/appointments?size=200').pipe(
      map(response => (response.content ?? []).filter(v => v.mode === 'IN_PERSON'))
    );
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

  // — Caricamento slot disponibili —

  loadAvailableSlots(doctorId: number, size: number = 100): Observable<AvailableSlotItem[]> {
    return this.api.request<SpringPage<AvailableSlotItem>>('GET', `/api/slots?doctorId=${doctorId}&size=${size}`).pipe(
      map(response => (response.content ?? []).filter(s => s.status === 'AVAILABLE' && s.mode === 'IN_PERSON'))
    );
  }

  loadRescheduleSlots(doctorId: number): Observable<AvailableSlotItem[]> {
    return this.api.request<SpringPage<AvailableSlotItem>>('GET', `/api/slots?doctorId=${doctorId}&size=200`).pipe(
      map(response => (response.content ?? []).filter(s => s.status === 'AVAILABLE' && s.mode === 'IN_PERSON'))
    );
  }

  // — Operazioni su visite —

  createVisit(payload: VisitCreatePayload): Observable<InPersonVisitItem> {
    return this.api.request<InPersonVisitItem>('POST', '/api/appointments', payload);
  }

  completeVisit(visitId: number): Observable<InPersonVisitItem> {
    return this.api.request<InPersonVisitItem>('POST', `/api/appointments/${visitId}/complete`);
  }

  cancelVisit(visitId: number): Observable<void> {
    return this.api.request<void>('DELETE', `/api/appointments/${visitId}`);
  }

  deleteVisit(visitId: number): Observable<void> {
    return this.api.request<void>('DELETE', `/api/appointments/${visitId}/force`);
  }

  rescheduleVisit(visitId: number, newSlotId: number): Observable<InPersonVisitItem> {
    return this.api.request<InPersonVisitItem>('PATCH', `/api/appointments/${visitId}/reschedule`, {
      newSlotId
    });
  }

  reassignVisit(visitId: number, newDoctorId: number, newSlotId: number): Observable<InPersonVisitItem> {
    return this.api.request<InPersonVisitItem>('PATCH', `/api/appointments/${visitId}/reassign`, {
      newDoctorId,
      newSlotId
    });
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
