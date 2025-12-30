package it.sanitech.scheduling.services.dto;

import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;

import java.time.Instant;

/**
 * DTO di lettura per lo slot.
 */
public record SlotDto(
        Long id,
        Long doctorId,
        String departmentCode,
        VisitMode mode,
        Instant startAt,
        Instant endAt,
        SlotStatus status
) { }
