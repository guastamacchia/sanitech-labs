import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import {
  TelevisitItem,
  LiveKitTokenResponse,
  TelevisitCreatePayload,
  DoctorItem,
  DoctorApiItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  PagedResponse
} from './dtos/televisit.dto';

@Injectable({ providedIn: 'root' })
export class TelevisitService {

  constructor(private api: ApiService) {}

  // — Caricamento televisite —

  loadTelevisits(): Observable<TelevisitItem[]> {
    return this.api.request<PagedResponse<TelevisitItem>>('GET', '/api/televisits').pipe(
      map(response => (response.content ?? []).map(tv => ({
        ...tv,
        provider: 'LIVEKIT',
        token: tv.roomName
      })))
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

  // — Pazienti con consenso televisita —

  loadPatientsWithTelevisitConsent(doctorId: number): Observable<number[]> {
    return this.api.request<number[]>('GET', `/api/consents/patients-with-televisit-consent?doctorId=${doctorId}`);
  }

  // — Operazioni su televisite —

  createTelevisit(payload: TelevisitCreatePayload): Observable<TelevisitItem> {
    return this.api.request<TelevisitItem>('POST', '/api/admin/televisits', payload).pipe(
      map(televisit => ({ ...televisit, provider: 'LIVEKIT', token: televisit.roomName }))
    );
  }

  startTelevisitSession(televisitId: number): Observable<TelevisitItem> {
    return this.api.request<TelevisitItem>('POST', `/api/televisits/${televisitId}/start`);
  }

  fetchDoctorToken(televisitId: number): Observable<LiveKitTokenResponse> {
    return this.api.request<LiveKitTokenResponse>('POST', `/api/televisits/${televisitId}/token/doctor`);
  }

  endTelevisit(televisitId: number): Observable<TelevisitItem> {
    return this.api.request<TelevisitItem>('POST', `/api/televisits/${televisitId}/end`);
  }

  deleteTelevisit(televisitId: number): Observable<void> {
    return this.api.request<void>('DELETE', `/api/admin/televisits/${televisitId}?force=true`);
  }

  cancelTelevisit(televisitId: number): Observable<TelevisitItem> {
    return this.api.request<TelevisitItem>('POST', `/api/televisits/${televisitId}/cancel`);
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
