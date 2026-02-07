// ============================================================================
// DTOs locali per il componente Clinical Docs
// ============================================================================

export type DocumentType = 'REFERTO_VISITA' | 'REFERTO_ESAME' | 'LETTERA_DIMISSIONE' | 'CERTIFICATO' | 'ALTRO';
export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface ClinicalDocument {
  id: string;
  patientId: number;
  patientName: string;
  type: DocumentType;
  title: string;
  description: string;
  department: string;
  uploadedBy: string;
  uploadedAt: string;
  status: DocumentStatus;
  fileUrl: string;
  isOwnDocument: boolean;
}

export interface UploadForm {
  patientId: number | null;
  type: DocumentType;
  title: string;
  description: string;
  file: File | null;
}
