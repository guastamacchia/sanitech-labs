package it.sanitech.payments.repositories.entities;

/**
 * Stato della prestazione sanitaria rispetto al pagamento.
 */
public enum ServicePerformedStatus {

    /**
     * In attesa di pagamento.
     */
    PENDING,

    /**
     * Pagamento completato.
     */
    PAID,

    /**
     * Prestazione gratuita (esenzione, convenzione, etc.).
     */
    FREE,

    /**
     * Prestazione annullata.
     */
    CANCELLED
}
