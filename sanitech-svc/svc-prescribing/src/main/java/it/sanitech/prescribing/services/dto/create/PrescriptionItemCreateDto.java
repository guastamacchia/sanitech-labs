package it.sanitech.prescribing.services.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizzato per creare una riga di prescrizione.
 */
public record PrescriptionItemCreateDto(

        @Size(max = 64)
        String medicationCode,

        @NotBlank
        @Size(max = 200)
        String medicationName,

        @NotBlank
        @Size(max = 100)
        String dosage,

        @NotBlank
        @Size(max = 80)
        String frequency,

        @Positive
        Integer durationDays,

        String instructions,

        Integer sortOrder
) { }
