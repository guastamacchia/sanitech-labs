// ============================================================================
// DTOs locali per il componente Televisits
// ============================================================================

import { TelevisitStatus as ApiTelevisitStatus } from '../../../../dtos/doctor-shared.dto';

/** Stati frontend usati nel componente UI */
export type TelevisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

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

/**
 * Converte lo stato backend (Java enum) nello stato frontend UI.
 * Backend: CREATED, SCHEDULED, ACTIVE, ENDED, CANCELED
 * Frontend: SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
 */
export function mapApiStatus(apiStatus: ApiTelevisitStatus): TelevisitStatus {
  switch (apiStatus) {
    case 'CREATED':
    case 'SCHEDULED':
      return 'SCHEDULED';
    case 'ACTIVE':
      return 'IN_PROGRESS';
    case 'ENDED':
      return 'COMPLETED';
    case 'CANCELED':
      return 'CANCELLED';
    default:
      return 'SCHEDULED';
  }
}
