// ============================================================================
// DTOs locali per il componente Admissions
// ============================================================================

export type AdmissionStatus = 'ACTIVE' | 'DISCHARGED' | 'TRANSFERRED';
export type AdmissionType = 'EMERGENCY' | 'SCHEDULED' | 'TRANSFER';

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
  bed: string;
  status: AdmissionStatus;
  isReferent: boolean;
  dailyNotes: DailyNote[];
}
