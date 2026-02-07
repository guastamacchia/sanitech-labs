import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import {
  AuditEventResponse,
  AuditEventDetail,
  AuditEventType,
  AuditActorType,
  AuditSubjectType,
  AuditOutcome,
  SpringPage
} from './dtos/audit.dto';

@Injectable({ providedIn: 'root' })
export class AuditService {

  constructor(private api: ApiService) {}

  // — Caricamento eventi audit —

  loadAllAuditEvents(): Observable<AuditEventDetail[]> {
    return this.api.get<SpringPage<AuditEventResponse>>('/api/audit/events', { size: '500' }).pipe(
      map(response => this.mapAuditEventsToDetail(response.content))
    );
  }

  // — Mappatura backend → UI —

  private mapAuditEventsToDetail(events: AuditEventResponse[]): AuditEventDetail[] {
    return events.map((event) => ({
      id: event.id,
      eventType: this.mapActionToEventType(event.action),
      action: event.action,
      actorType: (event.actorType as AuditActorType) || 'SYSTEM',
      actorId: event.actorId || 'system',
      actorName: event.actorId || 'system',
      subjectType: this.mapResourceToSubjectType(event.resourceType),
      subjectId: event.resourceId || `EVT-${event.id}`,
      resourceType: event.resourceType,
      resourceId: event.resourceId,
      resourceName: event.resourceType || '-',
      serviceName: event.source || 'unknown',
      ipAddress: event.ip,
      outcome: (event.outcome as AuditOutcome) || 'SUCCESS',
      occurredAt: event.occurredAt,
      integrityHash: event.traceId || this.generateHash()
    }));
  }

  private mapActionToEventType(action: string): AuditEventType {
    const a = action.toLowerCase();
    if (a.includes('consent') && a.includes('grant')) return 'CONSENT_GRANTED';
    if (a.includes('consent') && a.includes('revok')) return 'CONSENT_REVOKED';
    if (a.includes('document') || a.includes('view') || a.includes('read')) return 'DOCUMENT_ACCESS';
    if (a.includes('prescription')) return 'PRESCRIPTION_VIEW';
    if (a.includes('appointment') && a.includes('book')) return 'APPOINTMENT_BOOKED';
    if (a.includes('appointment') && a.includes('cancel')) return 'APPOINTMENT_CANCELLED';
    return 'PROFILE_UPDATE';
  }

  private mapResourceToSubjectType(resourceType?: string): AuditSubjectType {
    if (!resourceType) return 'DOCUMENT';
    const rt = resourceType.toUpperCase();
    if (rt.includes('PATIENT')) return 'PATIENT';
    if (rt.includes('DOCTOR')) return 'DOCTOR';
    if (rt.includes('DOCUMENT')) return 'DOCUMENT';
    if (rt.includes('PRESCRIPTION')) return 'PRESCRIPTION';
    if (rt.includes('APPOINTMENT')) return 'APPOINTMENT';
    if (rt.includes('CONSENT')) return 'CONSENT';
    return 'DOCUMENT';
  }

  private generateHash(): string {
    const chars = 'abcdef0123456789';
    let hash = '';
    for (let i = 0; i < 64; i++) {
      hash += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return hash;
  }
}
