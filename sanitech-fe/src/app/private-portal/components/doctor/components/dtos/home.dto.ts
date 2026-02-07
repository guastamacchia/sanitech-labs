// ============================================================================
// DTOs locali per il componente Home
// ============================================================================

export type UpcomingVisit = {
  id: number;
  department: string;
  patient: string;
  date: string;
  time: string;
  modality: string;
  reason: string;
  status: 'CONFIRMED' | 'PENDING';
};

export type SchedulingAppointment = {
  id: number;
  patientId: number;
  doctorId: number;
  slotId: number;
  departmentCode: string;
  mode: 'IN_PERSON' | 'TELEVISIT';
  startAt: string;
  endAt: string;
  status: string;
};

export type DoctorItem = {
  id: number;
  firstName: string;
  lastName: string;
  departmentCode?: string;
  facilityCode?: string;
};

export type PatientItem = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
};

export type DepartmentItem = {
  code: string;
  name: string;
};

export type PagedResponse<T> = {
  content?: T[];
};
