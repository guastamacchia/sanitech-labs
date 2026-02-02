import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../../../core/services/api.service';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Facility {
  id: number;
  code: string;
  name: string;
  address?: string;
  phone?: string;
}

export interface FacilityCreate {
  code: string;
  name: string;
  address?: string;
  phone?: string;
}

export interface Department {
  id: number;
  code: string;
  name: string;
  facilityCode: string;
  facilityName?: string;
  capacity?: number;
}

export interface DepartmentCreate {
  code: string;
  name: string;
  facilityCode: string;
  capacity?: number;
}

export interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialization?: string;
  status: 'ACTIVE' | 'PENDING' | 'DISABLED';
  createdAt?: string;
  activatedAt?: string;
  departmentCode: string;
  departmentName?: string;
  facilityCode: string;
  facilityName?: string;
}

export interface DoctorCreate {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialization?: string;
  departmentCode: string;
}

export interface DoctorUpdate {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  specialization?: string;
  departmentCode?: string;
}

export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  fiscalCode?: string;
  birthDate?: string;
  address?: string;
  status: 'ACTIVE' | 'PENDING' | 'DISABLED';
  registeredAt?: string;
  activatedAt?: string;
  departments?: Department[];
}

export interface PatientCreate {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  fiscalCode?: string;
  birthDate?: string;
  address?: string;
  departmentCodes?: string[];
}

export interface PatientUpdate {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  fiscalCode?: string;
  birthDate?: string;
  address?: string;
  departmentCodes?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DirectoryService {
  constructor(private api: ApiService) {}

  // Helper to wrap array response into Page format
  private wrapAsPage<T>(items: T[]): Page<T> {
    return {
      content: items,
      totalElements: items.length,
      totalPages: 1,
      number: 0,
      size: items.length
    };
  }

  // ========== FACILITIES ==========

  getFacilities(params?: { q?: string; page?: number; size?: number }): Observable<Page<Facility>> {
    // Backend returns List<Facility>, wrap it as Page
    return this.api.get<Facility[]>(`/api/admin/facilities`, { q: params?.q }).pipe(
      map(items => this.wrapAsPage(items))
    );
  }

  getFacility(id: number): Observable<Facility> {
    return this.api.get<Facility>(`/api/admin/facilities/${id}`);
  }

  createFacility(dto: FacilityCreate): Observable<Facility> {
    return this.api.post<Facility>(`/api/admin/facilities`, dto);
  }

  updateFacility(id: number, dto: Partial<FacilityCreate>): Observable<Facility> {
    // Backend uses @PutMapping, not PATCH
    return this.api.request<Facility>('PUT', `/api/admin/facilities/${id}`, dto);
  }

  deleteFacility(id: number): Observable<void> {
    return this.api.delete<void>(`/api/admin/facilities/${id}`);
  }

  // ========== DEPARTMENTS ==========

  getDepartments(params?: { q?: string; facility?: string; page?: number; size?: number }): Observable<Page<Department>> {
    // Backend returns List<Department>, wrap it as Page
    return this.api.get<Department[]>(`/api/admin/departments`, { q: params?.q }).pipe(
      map(items => this.wrapAsPage(items))
    );
  }

  getDepartment(id: number): Observable<Department> {
    return this.api.get<Department>(`/api/admin/departments/${id}`);
  }

  createDepartment(dto: DepartmentCreate): Observable<Department> {
    return this.api.post<Department>(`/api/admin/departments`, dto);
  }

  updateDepartment(id: number, dto: Partial<DepartmentCreate>): Observable<Department> {
    // Backend uses @PutMapping, not PATCH
    return this.api.request<Department>('PUT', `/api/admin/departments/${id}`, dto);
  }

  deleteDepartment(id: number): Observable<void> {
    return this.api.delete<void>(`/api/admin/departments/${id}`);
  }

  // ========== DOCTORS ==========

  getDoctors(params?: { q?: string; department?: string; facility?: string; status?: string; page?: number; size?: number }): Observable<Page<Doctor>> {
    return this.api.get<Page<Doctor>>(`/api/admin/doctors`, params);
  }

  getDoctor(id: number): Observable<Doctor> {
    return this.api.get<Doctor>(`/api/doctors/${id}`);
  }

  createDoctor(dto: DoctorCreate): Observable<Doctor> {
    return this.api.post<Doctor>(`/api/admin/doctors`, dto);
  }

  updateDoctor(id: number, dto: DoctorUpdate): Observable<Doctor> {
    return this.api.request<Doctor>('PATCH', `/api/admin/doctors/${id}`, dto);
  }

  deleteDoctor(id: number): Observable<void> {
    return this.api.delete<void>(`/api/admin/doctors/${id}`);
  }

  activateDoctor(id: number): Observable<Doctor> {
    return this.api.request<Doctor>('PATCH', `/api/admin/doctors/${id}/activate`, {});
  }

  disableDoctor(id: number): Observable<Doctor> {
    return this.api.request<Doctor>('PATCH', `/api/admin/doctors/${id}/disable`, {});
  }

  resendDoctorActivation(id: number): Observable<void> {
    return this.api.post<void>(`/api/admin/doctors/${id}/resend-activation`, {});
  }

  transferDoctor(id: number, departmentCode: string): Observable<Doctor> {
    return this.api.request<Doctor>('PATCH', `/api/admin/doctors/${id}/transfer?departmentCode=${departmentCode}`, {});
  }

  // ========== PATIENTS ==========

  getPatients(params?: { q?: string; department?: string; status?: string; page?: number; size?: number }): Observable<Page<Patient>> {
    return this.api.get<Page<Patient>>(`/api/admin/patients`, params);
  }

  getPatient(id: number): Observable<Patient> {
    return this.api.get<Patient>(`/api/patients/${id}`);
  }

  createPatient(dto: PatientCreate): Observable<Patient> {
    return this.api.post<Patient>(`/api/admin/patients`, dto);
  }

  updatePatient(id: number, dto: PatientUpdate): Observable<Patient> {
    return this.api.request<Patient>('PATCH', `/api/admin/patients/${id}`, dto);
  }

  deletePatient(id: number): Observable<void> {
    return this.api.delete<void>(`/api/admin/patients/${id}`);
  }

  activatePatient(id: number): Observable<Patient> {
    return this.api.request<Patient>('PATCH', `/api/admin/patients/${id}/activate`, {});
  }

  disablePatient(id: number): Observable<Patient> {
    return this.api.request<Patient>('PATCH', `/api/admin/patients/${id}/disable`, {});
  }

  resendPatientActivation(id: number): Observable<void> {
    return this.api.post<void>(`/api/admin/patients/${id}/resend-activation`, {});
  }
}
