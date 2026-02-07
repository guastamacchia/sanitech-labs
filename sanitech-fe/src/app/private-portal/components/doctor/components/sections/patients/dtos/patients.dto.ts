// ============================================================================
// DTOs locali per il componente Patients
// ============================================================================

import type { ConsentScope } from '../../../../dtos/doctor-shared.dto';

export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  fiscalCode: string;
  email: string;
  phone: string;
  birthDate: string;
  consents: ConsentScope[];
  lastAccess?: string;
  nextAppointment?: string;
  hasActiveConsent: boolean;
}
