package it.sanitech.televisit.services.dto;

import it.sanitech.televisit.repositories.entities.TelevisitStatus;

import java.time.OffsetDateTime;

/**
 * DTO di lettura per una sessione di video-visita.
 */
public record TelevisitDto(

        /** Identificatore univoco della sessione. */
        Long id,

        /** Nome room LiveKit. */
        String roomName,

        /** Reparto della sessione. */
        String department,

        /** Subject Keycloak del medico. */
        String doctorSubject,

        /** Subject Keycloak del paziente. */
        String patientSubject,

        /** Data/ora prevista. */
        OffsetDateTime scheduledAt,

        /** Stato corrente. */
        TelevisitStatus status,

        /** Note cliniche del medico. */
        String notes,

        /** Nome completo del paziente (arricchito da svc-directory, può essere null). */
        String patientName,

        /** Nome completo del medico (arricchito da svc-directory, può essere null). */
        String doctorName

) {
}
