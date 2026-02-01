package it.sanitech.audit.services.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * DTO di lettura per esporre un evento audit.
 */
public record AuditEventDto(
        Long id,
        Instant occurredAt,
        String source,
        String actorType,
        String actorId,
        String action,
        String resourceType,
        String resourceId,
        String outcome,
        String ip,
        String traceId,
        JsonNode details
) { }
