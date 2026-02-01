package it.sanitech.prescribing.services.dto;

import java.lang.Integer;

/**
 * DTO di lettura per una singola riga di prescrizione.
 */
public record PrescriptionItemDto(
        Long id,
        String medicationCode,
        String medicationName,
        String dosage,
        String frequency,
        Integer durationDays,
        String instructions,
        Integer sortOrder
) { }
