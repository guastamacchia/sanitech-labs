package it.sanitech.prescribing.services.dto.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO utilizzato per creare una nuova prescrizione.
 */
public record PrescriptionCreateDto(

        @Positive
        Long patientId,

        @NotBlank
        @Size(max = 80)
        String departmentCode,

        String notes,

        @NotEmpty
        List<@Valid PrescriptionItemCreateDto> items
) { }
