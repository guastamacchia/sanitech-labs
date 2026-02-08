// ============================================================================
// DTOs locali per il componente Profile
// ============================================================================

export interface DoctorProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  department: string;
  facility: string;
  specialization: string;
}

export interface Availability {
  day: string;
  morning: string;
  afternoon: string;
}

export interface NotificationPreferences {
  emailConsents: boolean;
  emailAppointments: boolean;
  emailDocuments: boolean;
  emailAdmissions: boolean;
}

export interface DoctorStats {
  activePatients: number;
  monthlyAppointments: number;
  activeConsents: number;
  pendingTelevisits: number;
}
