package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.PrivacyConsentType;

import java.time.Instant;

/**
 * DTO di lettura per i consensi privacy.
 */
public record PrivacyConsentDto(
        Long id,
        Long patientId,
        PrivacyConsentType consentType,
        boolean accepted,
        Instant signedAt
) { }
