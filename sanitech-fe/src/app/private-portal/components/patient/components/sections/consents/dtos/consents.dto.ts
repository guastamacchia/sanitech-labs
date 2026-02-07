// ============================================================
// DTOs per Consent Management
// Estratti da consent-management.component.ts
// ============================================================

export type ConsentScope = 'DOCS' | 'PRESCRIPTIONS' | 'TELEVISIT';
export type ConsentStatus = 'GRANTED' | 'REVOKED';

/** DTO dal backend: /api/consents/me/doctors */
export interface ConsentDto {
  id: number;
  patientId: number;
  doctorId: number;
  scope: ConsentScope;
  status: ConsentStatus;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
  updatedAt: string;
}

/** DTO dal backend: /api/doctors */
export interface DoctorDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentCode: string;
  departmentName: string;
  facilityCode: string;
  facilityName: string;
}

/** Risposta paginata dal backend */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/** Modello arricchito per la visualizzazione */
export interface DoctorConsent {
  id: number;
  doctorId: number;
  doctorName: string;
  doctorSpecialty: string;
  scope: ConsentScope;
  status: ConsentStatus;
  grantedAt: string;
  revokedAt?: string;
  expiresAt?: string;
}
