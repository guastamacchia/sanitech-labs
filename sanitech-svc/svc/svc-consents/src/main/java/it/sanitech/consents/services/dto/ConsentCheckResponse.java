package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.ConsentStatus;

import java.time.Instant;

/**
 * Risposta sintetica per la verifica del consenso (endpoint "check").
 */
public record ConsentCheckResponse(
        Long patientId,
        Long doctorId,
        ConsentScope scope,
        boolean allowed,
        ConsentStatus status,
        Instant expiresAt
) { }
