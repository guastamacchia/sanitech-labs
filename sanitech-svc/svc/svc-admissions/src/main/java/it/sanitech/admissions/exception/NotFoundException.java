package it.sanitech.admissions.exception;

/**
 * Eccezione applicativa che indica che una risorsa non è stata trovata.
 *
 * <p>
 * Viene sollevata dai servizi di dominio e trasformata in risposta HTTP 404
 * dal {@link GlobalExceptionHandler}.
 * </p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory per creare un messaggio uniforme "X con id Y non trovata".
     */
    public static NotFoundException of(String resourceName, Object id) {
        return new NotFoundException(resourceName + " con id " + id + " non trovata.");
    }
}
