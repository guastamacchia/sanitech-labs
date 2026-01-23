package it.sanitech.prescribing.services.dto;

import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;

import java.time.Instant;
import java.util.List;

/**
 * DTO di lettura per una prescrizione.
 *
 * <p>
 * Include le righe ({@link PrescriptionItemDto}) per evitare round-trip aggiuntivi lato client.
 * </p>
 */
public record PrescriptionDto(
        Long id,
        Long patientId,
        Long doctorId,
        String departmentCode,
        PrescriptionStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        Instant issuedAt,
        Instant cancelledAt,
        List<PrescriptionItemDto> items
) { }
