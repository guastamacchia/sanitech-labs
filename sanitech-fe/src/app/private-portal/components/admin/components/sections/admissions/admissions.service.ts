import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import {
  AdmissionItem,
  CapacityItem,
  DoctorItem,
  DoctorApiItem,
  PatientItem,
  FacilityItem,
  DepartmentItem,
  PagedResponse,
  AdmissionCreatePayload
} from './dtos/admissions.dto';

@Injectable({ providedIn: 'root' })
export class AdmissionsService {

  constructor(private api: ApiService) {}

  // — Caricamento ricoveri —

  loadAdmissions(): Observable<AdmissionItem[]> {
    return this.api.request<PagedResponse<AdmissionItem>>('GET', '/api/admissions').pipe(
      map(response => (response.content ?? []).map(adm => ({
        ...adm,
        department: adm.departmentCode
      })))
    );
  }

  // — Caricamento capacità reparti —

  loadDepartmentCapacity(): Observable<CapacityItem[]> {
    return this.api.request<CapacityItem[]>('GET', '/api/departments/capacity');
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
      map(departments => (departments ?? []).map(dept => ({
        id: dept.id,
        code: dept.code,
        name: dept.name,
        facilityCode: dept.facilityCode ?? dept.facility_code ?? dept.facility?.code ?? undefined
      } as DepartmentItem)))
    );
  }

  // — Pazienti con consenso per un medico —

  loadPatientsWithConsent(doctorId: number): Observable<number[]> {
    return this.api.request<number[]>('GET', `/api/consents/patients-with-consent?scope=RECORDS&doctorId=${doctorId}`);
  }

  // — Medici con consenso per un paziente —

  loadDoctorsWithConsent(patientId: number): Observable<number[]> {
    return this.api.get<number[]>('/api/consents/doctors-with-consent', { patientId });
  }

  // — Operazioni su ricoveri —

  createAdmission(payload: AdmissionCreatePayload): Observable<AdmissionItem> {
    return this.api.request<AdmissionItem>('POST', '/api/admissions', payload).pipe(
      map(admission => ({ ...admission, department: admission.departmentCode }))
    );
  }

  updateAdmission(admissionId: number, patch: Partial<AdmissionItem>): Observable<AdmissionItem> {
    return this.api.request<AdmissionItem>('PATCH', `/api/admissions/${admissionId}`, patch);
  }

  dischargeAdmission(admissionId: number): Observable<AdmissionItem> {
    return this.api.request<AdmissionItem>('POST', `/api/admissions/${admissionId}/discharge`, {});
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
