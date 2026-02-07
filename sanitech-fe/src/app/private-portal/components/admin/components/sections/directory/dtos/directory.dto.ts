// DTO per la sezione directory admin

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
  doctorCount?: number;
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
