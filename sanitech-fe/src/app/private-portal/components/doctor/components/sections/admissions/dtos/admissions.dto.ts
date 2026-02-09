// ============================================================================
// DTOs locali per il componente Admissions
// BUG-001/007: Allineati con backend Java enums
// ============================================================================

export type AdmissionStatus = 'ACTIVE' | 'DISCHARGED' | 'CANCELLED';
export type AdmissionType = 'INPATIENT' | 'DAY_HOSPITAL' | 'OBSERVATION';

export interface DailyNote {
  date: string;
  note: string;
  author: string;
}

export interface Admission {
  id: number;
  patientId: number;
  patientName: string;
  patientAge: number;
  admittedAt: string;
  dischargedAt?: string;
  type: AdmissionType;
  diagnosis: string;
  status: AdmissionStatus;
  isReferent: boolean;
  attendingDoctorId?: number;
  dailyNotes: DailyNote[];
}
