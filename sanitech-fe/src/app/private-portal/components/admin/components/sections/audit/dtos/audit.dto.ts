// DTO per la sezione audit

// DTO backend AuditEventDto
export interface AuditEventResponse {
  id: number;
  occurredAt: string;
  source: string;
  actorType: string;
  actorId: string;
  action: string;
  resourceType?: string;
  resourceId?: string;
  outcome: string;
  ip?: string;
  traceId?: string;
  details?: Record<string, unknown>;
}

// Risposta paginata Spring Boot
export interface SpringPage<T> {
  content: T[];
  pageable: { pageNumber: number; pageSize: number };
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Evento audit mappato per la UI
export type AuditEventType =
  | 'CONSENT_GRANTED'
  | 'CONSENT_REVOKED'
  | 'DOCUMENT_ACCESS'
  | 'PRESCRIPTION_VIEW'
  | 'APPOINTMENT_BOOKED'
  | 'APPOINTMENT_CANCELLED'
  | 'PROFILE_UPDATE';

export type AuditActorType = 'USER' | 'SERVICE' | 'SYSTEM' | 'ANONYMOUS';

export type AuditSubjectType = 'PATIENT' | 'DOCTOR' | 'ADMIN' | 'DOCUMENT' | 'PRESCRIPTION' | 'APPOINTMENT' | 'CONSENT';

export type AuditOutcome = 'SUCCESS' | 'DENIED' | 'FAILURE';

export interface AuditEventDetail {
  id: number;
  eventType: AuditEventType;
  action: string;
  actorType: AuditActorType;
  actorId: string;
  actorName: string;
  actorRole?: string;
  subjectType: AuditSubjectType;
  subjectId: string;
  resourceType?: string;
  resourceId?: string;
  resourceName?: string;
  consentScope?: string;
  consentValidAtAccess?: boolean;
  serviceName: string;
  ipAddress?: string;
  outcome: AuditOutcome;
  outcomeReason?: string;
  occurredAt: string;
  integrityHash: string;
}
