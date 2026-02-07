// DTO per la sezione visite in presenza admin
import { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse } from '../../../../dtos/admin-shared.dto';

// Re-export per comodita'
export { DoctorItem, DoctorApiItem, PatientItem, FacilityItem, DepartmentItem, PagedResponse };

// Allineato a AppointmentDto backend (per admin visite in presenza)
export interface InPersonVisitItem {
  id: number;
  slotId: number;
  patientId: number;
  doctorId: number;
  departmentCode: string;
  mode: 'IN_PERSON' | 'TELEVISIT';
  startAt: string;
  endAt: string;
  status: 'BOOKED' | 'COMPLETED' | 'CANCELLED';
  reason?: string;
}

// Slot disponibile per prenotazione visita in presenza
export interface AvailableSlotItem {
  id: number;
  doctorId: number;
  departmentCode: string;
  mode: string;
  startAt: string;
  endAt: string;
  status: string;
}

// Risposta paginata Spring Boot
export interface SpringPage<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Payload per creazione visita in presenza
export interface VisitCreatePayload {
  slotId: number;
  patientId: number;
  reason: string | null;
}
