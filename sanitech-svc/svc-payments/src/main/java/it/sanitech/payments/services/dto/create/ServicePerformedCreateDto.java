package it.sanitech.payments.services.dto.create;

import it.sanitech.payments.repositories.entities.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * DTO per la creazione manuale di una prestazione sanitaria (admin).
 */
public record ServicePerformedCreateDto(

        @NotNull(message = "Il medico è obbligatorio")
        Long doctorId,

        @NotNull(message = "Il paziente è obbligatorio")
        Long patientId,

        @NotNull(message = "Il tipo di pagamento è obbligatorio")
        PaymentType paymentType,

        @NotBlank(message = "La descrizione è obbligatoria")
        @Size(max = 500)
        String description,

        @Min(value = 1, message = "L'importo deve essere maggiore di zero")
        long amountCents,

        @NotNull(message = "La data della prestazione è obbligatoria")
        Instant performedAt

) {}
