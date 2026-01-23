package it.sanitech.televisit.services.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * DTO per creare una nuova sessione di video-visita.
 */
public record TelevisitCreateDto(

        /** Subject/identity Keycloak del medico. */
        @NotBlank
        String doctorSubject,

        /** Subject/identity Keycloak del paziente. */
        @NotBlank
        String patientSubject,

        /** Reparto della sessione (codice). */
        @NotBlank
        String department,

        /** Data/ora prevista della sessione. */
        @NotNull
        OffsetDateTime scheduledAt

) {
}
