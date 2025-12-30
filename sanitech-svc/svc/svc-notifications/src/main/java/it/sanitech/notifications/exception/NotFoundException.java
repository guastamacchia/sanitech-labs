package it.sanitech.notifications.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 *
 * <p>Viene intercettata dal {@link GlobalExceptionHandler} e convertita in Problem Details (RFC 7807).</p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire un messaggio coerente lato dominio.
     *
     * @param entity nome della risorsa (es. "Notifica")
     * @param id     identificativo ricercato
     * @return eccezione pronta da lanciare
     */
    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " con id " + id + " non trovata.");
    }
}
