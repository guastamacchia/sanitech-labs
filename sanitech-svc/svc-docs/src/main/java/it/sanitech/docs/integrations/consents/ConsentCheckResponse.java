package it.sanitech.docs.integrations.consents;

import java.time.Instant;

/**
 * DTO di risposta per la verifica consenso lato {@code svc-consents}.
 *
 * @param patientId id del paziente
 * @param doctorId id del medico
 * @param scope ambito del consenso
 * @param allowed true se il consenso è valido
 * @param status stato del consenso
 * @param expiresAt data di scadenza del consenso
 */
public record ConsentCheckResponse(
        Long patientId,
        Long doctorId,
        String scope,
        boolean allowed,
        String status,
        Instant expiresAt
) { }
