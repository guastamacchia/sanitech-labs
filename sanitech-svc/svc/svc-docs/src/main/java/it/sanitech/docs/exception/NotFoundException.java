package it.sanitech.docs.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 *
 * <p>
 * Tipicamente lanciata dai servizi di dominio e gestita da {@link GlobalExceptionHandler}
 * con risposta RFC 7807 (HTTP 404).
 * </p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire un messaggio consistente in base a tipo e id.
     *
     * @param obj nome logico della risorsa (es. "Documento")
     * @param id  identificatore ricercato
     * @return eccezione pronta da lanciare
     */
    public static NotFoundException of(String obj, Object id) {
        return new NotFoundException(obj + " con id " + id + " non trovato.");
    }
}
