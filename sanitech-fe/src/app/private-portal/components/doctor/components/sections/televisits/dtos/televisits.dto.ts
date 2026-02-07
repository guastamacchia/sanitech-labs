// ============================================================================
// DTOs locali per il componente Televisits
// ============================================================================

export type TelevisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';

export interface Televisit {
  id: number;
  patientId?: number;
  patientSubject: string;
  patientName: string;
  scheduledAt: string;
  duration: number;
  reason: string;
  status: TelevisitStatus;
  roomUrl?: string;
  notes?: string;
  documents?: { id: number; name: string; type: string }[];
}
