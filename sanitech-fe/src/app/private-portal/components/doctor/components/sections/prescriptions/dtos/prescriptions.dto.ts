// ============================================================================
// DTOs locali per il componente Prescriptions
// ============================================================================

export type PrescriptionStatus = 'DRAFT' | 'ISSUED' | 'EXPIRED' | 'CANCELLED';

export interface Medication {
  name: string;
  dosage: string;
  posology: string;
  duration: number;
  notes: string;
}

export interface Prescription {
  id: number;
  patientId: number;
  patientName: string;
  medications: Medication[];
  issuedAt: string;
  expiresAt: string;
  status: PrescriptionStatus;
  notes: string;
  pdfUrl: string;
}

export interface PrescriptionForm {
  patientId: number | null;
  medications: Medication[];
  notes: string;
}

export interface DrugSuggestion {
  name: string;
  dosages: string[];
}
