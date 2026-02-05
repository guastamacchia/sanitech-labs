package it.sanitech.payments.services.dto;

import it.sanitech.payments.repositories.entities.PaymentType;
import it.sanitech.payments.repositories.entities.ServicePerformedStatus;
import it.sanitech.payments.repositories.entities.ServiceSourceType;
import it.sanitech.payments.repositories.entities.ServiceType;

import java.time.Instant;

/**
 * DTO per rappresentare una prestazione sanitaria.
 */
public record ServicePerformedDto(
        Long id,
        ServiceType serviceType,
        PaymentType paymentType,
        ServiceSourceType sourceType,
        Long sourceId,
        Long patientId,
        String patientSubject,
        String patientName,
        String patientEmail,
        Long doctorId,
        String doctorName,
        String departmentCode,
        String description,
        long amountCents,
        String currency,
        ServicePerformedStatus status,
        Instant performedAt,
        Instant startedAt,
        Integer daysCount,
        int reminderCount,
        Instant lastReminderAt,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
