package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.PrivacyConsentType;
import jakarta.validation.constraints.NotNull;

/**
 * DTO per registrare un consenso privacy (GDPR, privacy, terapia).
 */
public record PrivacyConsentCreateDto(
        @NotNull PrivacyConsentType consentType,
        @NotNull Boolean accepted
) { }
