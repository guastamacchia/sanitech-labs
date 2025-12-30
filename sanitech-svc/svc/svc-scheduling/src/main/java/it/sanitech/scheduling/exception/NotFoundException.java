package it.sanitech.scheduling.exception;

/**
 * Eccezione applicativa per indicare che una risorsa non è stata trovata.
 * <p>
 * L'eccezione viene sollevata dai servizi di dominio e trasformata in una
 * risposta RFC 7807 dal {@link GlobalExceptionHandler}.
 * </p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory method per costruire eccezioni con messaggio coerente.
     *
     * @param entity nome dell'entità (es. "Slot", "Appointment")
     * @param id     identificativo ricercato
     * @return eccezione {@link NotFoundException} pronta per essere lanciata
     */
    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " con id " + id + " non trovata.");
    }
}
