package it.sanitech.payments.repositories.entities;

/**
 * Tipo di pagamento per le prestazioni sanitarie.
 */
public enum PaymentType {

    /**
     * Visita medica.
     */
    VISITA,

    /**
     * Ricovero ospedaliero.
     */
    RICOVERO,

    /**
     * Altro tipo di prestazione.
     */
    ALTRO
}
