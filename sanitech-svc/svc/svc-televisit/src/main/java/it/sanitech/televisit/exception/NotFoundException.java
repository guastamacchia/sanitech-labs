package it.sanitech.televisit.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata (HTTP 404).
 *
 * <p>Viene intercettata da {@link GlobalExceptionHandler} e resa in formato RFC 7807.</p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory per costruire messaggi coerenti (es. "TelevisitSession con id 123 non trovata.").
     *
     * @param objectName nome logico della risorsa
     * @param id         identificatore ricercato
     * @return istanza di {@link NotFoundException}
     */
    public static NotFoundException of(String objectName, Object id) {
        return new NotFoundException(objectName + " con id " + id + " non trovata.");
    }
}
