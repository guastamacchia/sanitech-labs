package it.sanitech.payments.repositories.entities;

/**
 * Tipo di sorgente che ha generato la prestazione.
 */
public enum ServiceSourceType {

    /**
     * Televisita (videoconsulto).
     */
    TELEVISIT,

    /**
     * Ricovero ospedaliero.
     */
    ADMISSION,

    /**
     * Visita in presenza (appuntamento completato).
     */
    APPOINTMENT
}
