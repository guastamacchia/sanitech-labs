package it.sanitech.prescribing.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 *
 * <p>
 * Viene sollevata dal livello di service e tradotta in HTTP 404 dal {@link GlobalExceptionHandler}.
 * </p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire eccezioni semantiche.
     *
     * @param obj nome della risorsa (es. "Prescrizione")
     * @param id identificativo ricercato
     */
    public static NotFoundException of(String obj, Object id) {
        return new NotFoundException(obj + " con id " + id + " non trovata.");
    }
}
