package it.sanitech.admissions.services.dto.update;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO per impostare/aggiornare la capacità posti letto di un reparto.
 */
public record CapacityUpsertDto(
        @NotNull
        @Min(0)
        Integer totalBeds
) { }
