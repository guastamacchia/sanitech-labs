package it.sanitech.consents.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 * Intercettata dal {@link GlobalExceptionHandler} e resa come RFC 7807 (404).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire messaggi consistenti.
     */
    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " con id " + id + " non trovata.");
    }
}
