package it.sanitech.consents.services.dto;

import it.sanitech.consents.repositories.entities.ConsentScope;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;

/**
 * DTO per concedere consensi multipli (tutti gli scope selezionati) per un singolo medico.
 * Ogni scope genera un record separato in tabella.
 */
public record ConsentBulkCreateDto(
        @NotNull Long doctorId,
        @NotEmpty Set<ConsentScope> scopes,
        Instant expiresAt
) { }
