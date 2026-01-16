package it.sanitech.docs.integrations.consents;

/**
 * DTO di risposta per la verifica consenso lato {@code svc-consents}.
 *
 * @param allowed true se il consenso è valido per la purpose richiesta
 * @param reason eventuale motivazione/nota
 */
public record ConsentCheckResponse(boolean allowed, String reason) { }
