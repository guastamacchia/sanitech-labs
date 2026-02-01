package it.sanitech.notifications.services.dto;

import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.NotificationStatus;
import it.sanitech.notifications.repositories.entities.RecipientType;

import java.time.Instant;

/**
 * DTO di lettura per esporre i dati di una notifica via API.
 */
public record NotificationDto(
        Long id,
        RecipientType recipientType,
        String recipientId,
        NotificationChannel channel,
        String toAddress,
        String subject,
        String body,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt,
        String errorMessage
) { }
