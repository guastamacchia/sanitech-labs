package it.sanitech.scheduling.services.dto.create;

import it.sanitech.scheduling.repositories.entities.VisitMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTO di input per creare uno slot.
 */
public record SlotCreateDto(
        @NotNull Long doctorId,
        @NotBlank String departmentCode,
        @NotNull VisitMode mode,
        @NotNull Instant startAt,
        @NotNull Instant endAt
) { }
