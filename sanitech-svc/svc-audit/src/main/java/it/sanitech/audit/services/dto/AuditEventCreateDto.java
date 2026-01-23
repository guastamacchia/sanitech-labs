package it.sanitech.audit.services.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO per registrare un evento audit via API.
 * <p>
 * Nota: campi come {@code actorId} e {@code traceId} possono essere derivati
 * dal contesto (JWT/MDC) nel service.
 * </p>
 */
public record AuditEventCreateDto(

        @NotBlank String action,

        String resourceType,

        String resourceId,

        /**
         * Esito dell'azione (es. SUCCESS/DENIED/FAILURE).
         * In assenza, il service applica {@code SUCCESS}.
         */
        String outcome,

        /**
         * Dettagli strutturati dell'evento (serializzati in JSONB su DB).
         */
        Object details

) { }
