package it.sanitech.payments.repositories.entities;

/**
 * Metodo di pagamento.
 *
 * <p>
 * In questa versione sono presenti metodi generici; eventuali provider specifici
 * possono essere gestiti tramite webhook o integrazioni dedicate.
 * </p>
 */
public enum PaymentMethod {
    CARD,
    BANK_TRANSFER,
    CASH
}
