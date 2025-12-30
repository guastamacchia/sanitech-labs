package it.sanitech.payments.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 * Utilizzata dai servizi di dominio e intercettata dal {@link GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire eccezioni semantiche e uniformi.
     *
     * @param obj nome logico della risorsa (es. "PaymentOrder")
     * @param id identificativo richiesto
     * @return istanza di {@link NotFoundException}
     */
    public static NotFoundException of(String obj, Object id) {
        return new NotFoundException(obj + " con id " + id + " non trovata.");
    }
}
