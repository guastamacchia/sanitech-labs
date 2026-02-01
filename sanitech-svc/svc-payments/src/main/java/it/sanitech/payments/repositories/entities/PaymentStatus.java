package it.sanitech.payments.repositories.entities;

/**
 * Stato dell'ordine di pagamento.
 */
public enum PaymentStatus {
    CREATED,
    CAPTURED,
    FAILED,
    CANCELLED,
    REFUNDED
}
