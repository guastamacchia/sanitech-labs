package it.sanitech.consents.services.dto;

import java.time.Instant;

/**
 * DTO per aggiornare un consenso esistente (modifica scadenza).
 */
public record ConsentUpdateDto(
        Instant expiresAt
) { }
