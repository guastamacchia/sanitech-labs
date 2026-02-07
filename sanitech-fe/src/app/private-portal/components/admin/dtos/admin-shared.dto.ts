// DTO condivisi tra le sezioni admin (directory, admissions, televisit, in-person-visits, payments)

export interface DoctorItem {
  id: number;
  firstName: string;
  lastName: string;
  departmentCode?: string;
  facilityCode?: string;
  email?: string;
  phone?: string;
}

export interface DoctorApiItem {
  id: number;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  departmentCode?: string;
  facilityCode?: string;
  departments?: Array<{ code: string; name: string }>;
}

export interface PatientItem {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
}

export interface FacilityItem {
  id?: number;
  code: string;
  name: string;
}

export interface DepartmentItem {
  id?: number;
  code: string;
  name: string;
  facilityCode?: string;
}

export interface PagedResponse<T> {
  content?: T[];
}
