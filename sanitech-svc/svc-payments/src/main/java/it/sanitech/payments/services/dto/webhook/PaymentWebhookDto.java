package it.sanitech.payments.services.dto.webhook;

import it.sanitech.payments.repositories.entities.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO per notifiche provider (webhook).
 */
public record PaymentWebhookDto(

        @NotBlank
        String provider,

        @NotBlank
        @Size(max = 128)
        String providerReference,

        @NotNull
        PaymentStatus status

) { }
