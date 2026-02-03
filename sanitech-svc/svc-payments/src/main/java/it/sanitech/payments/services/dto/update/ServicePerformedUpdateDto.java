package it.sanitech.payments.services.dto.update;

import it.sanitech.payments.repositories.entities.ServicePerformedStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento parziale di una prestazione sanitaria.
 */
public record ServicePerformedUpdateDto(
        @Min(0)
        Long amountCents,

        ServicePerformedStatus status,

        @Size(max = 500)
        String notes,

        @Size(max = 255)
        String patientName,

        @Size(max = 255)
        String patientEmail
) {}
