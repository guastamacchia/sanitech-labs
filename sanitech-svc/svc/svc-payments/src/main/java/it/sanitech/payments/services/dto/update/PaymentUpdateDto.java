package it.sanitech.payments.services.dto.update;

import it.sanitech.payments.repositories.entities.PaymentStatus;

/**
 * DTO per aggiornamento parziale (PATCH) di un ordine (lato admin).
 *
 * <p>
 * I campi null vengono ignorati dal mapper MapStruct (IGNORE), consentendo patch incrementali.
 * </p>
 */
public record PaymentUpdateDto(
        PaymentStatus status,
        String providerReference,
        String description
) { }
