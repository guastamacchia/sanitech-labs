package it.sanitech.payments.services.dto.create;

import it.sanitech.payments.repositories.entities.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO per la creazione di un ordine di pagamento (lato paziente).
 *
 * <p>
 * Il {@code patientId} non viene accettato dal client: viene derivato dal claim {@code pid}.
 * </p>
 */
public record PaymentCreateDto(

        @NotNull
        Long appointmentId,

        @Min(1)
        long amountCents,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        PaymentMethod method,

        @Size(max = 255)
        String description

) { }
