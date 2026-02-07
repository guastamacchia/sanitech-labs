// DTO per la sezione ricoveri admin
import { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse } from '../../../../dtos/admin-shared.dto';

// Re-export per comodità
export { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse };

// Allineato a AdmissionDto backend
export interface AdmissionItem {
  id: number;
  patientId: number;
  departmentCode: string;
  admissionType: 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION';
  status: 'ACTIVE' | 'DISCHARGED' | 'CANCELLED';
  admittedAt: string;
  dischargedAt?: string;
  notes?: string;
  attendingDoctorId?: number;
  department?: string;
}

// Allineato a CapacityDto backend
export interface CapacityItem {
  departmentCode: string;
  totalBeds: number;
  occupiedBeds: number;
  availableBeds: number;
  updatedAt: string;
}

// Payload per creazione ricovero
export interface AdmissionCreatePayload {
  patientId: number;
  departmentCode: string;
  admissionType: 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION';
  notes: string | null;
  attendingDoctorId: number;
}
