package it.sanitech.scheduling.services.dto;

import it.sanitech.scheduling.repositories.entities.AppointmentStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;

import java.time.Instant;

/**
 * DTO di lettura per l'appuntamento.
 */
public record AppointmentDto(
        Long id,
        Long slotId,
        Long patientId,
        Long doctorId,
        String departmentCode,
        VisitMode mode,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status,
        String reason
) { }
