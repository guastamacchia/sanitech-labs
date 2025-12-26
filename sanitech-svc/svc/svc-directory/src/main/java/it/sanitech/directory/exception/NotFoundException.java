package it.sanitech.directory.exception;

/**
 * Eccezione applicativa che indica l'assenza di una risorsa richiesta (HTTP 404).
 *
 * <p>
 * Viene sollevata tipicamente dal layer Service quando una ricerca per id/codice
 * non produce risultati. La traduzione in risposta HTTP avviene nel {@link GlobalExceptionHandler}.
 * </p>
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Factory per creare messaggi coerenti nelle lookup per identificatore.
     *
     * @param entity nome logico dell'entità (es. "Medico")
     * @param id     identificatore ricercato
     */
    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " con id " + id + " non trovato.");
    }
}
