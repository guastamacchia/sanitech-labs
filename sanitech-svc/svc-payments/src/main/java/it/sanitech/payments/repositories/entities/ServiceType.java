package it.sanitech.payments.repositories.entities;

/**
 * Tipo di prestazione sanitaria.
 */
public enum ServiceType {

    /**
     * Visita medica (televisita).
     * Importo di default: 100 EUR.
     */
    MEDICAL_VISIT,

    /**
     * Ricovero ospedaliero.
     * Importo di default: 20 EUR al giorno.
     */
    HOSPITALIZATION
}
