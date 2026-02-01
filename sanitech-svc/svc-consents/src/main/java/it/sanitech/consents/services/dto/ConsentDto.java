package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.ConsentStatus;

import java.time.Instant;

/**
 * DTO di lettura per esporre i consensi.
 */
public record ConsentDto(
        Long id,
        Long patientId,
        Long doctorId,
        ConsentScope scope,
        ConsentStatus status,
        Instant grantedAt,
        Instant revokedAt,
        Instant expiresAt,
        Instant updatedAt
) { }
