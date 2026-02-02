package it.sanitech.payments.services.dto;

import it.sanitech.payments.repositories.entities.PaymentMethod;
import it.sanitech.payments.repositories.entities.PaymentStatus;

import java.time.Instant;

/**
 * DTO di lettura per esporre un ordine di pagamento via API.
 */
public record PaymentOrderDto(
        Long id,
        Long appointmentId,
        Long patientId,
        String patientEmail,
        String patientName,
        long amountCents,
        String currency,
        PaymentMethod method,
        String provider,
        String providerReference,
        PaymentStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) { }
