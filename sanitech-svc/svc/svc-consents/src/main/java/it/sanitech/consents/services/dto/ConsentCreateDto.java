package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.ConsentScope;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTO per concedere un consenso (tipicamente da parte del paziente).
 */
public record ConsentCreateDto(
        @NotNull Long doctorId,
        @NotNull ConsentScope scope,
        Instant expiresAt
) { }
