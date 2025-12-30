package it.sanitech.admissions.exception;

/**
 * Eccezione applicativa per conflitti di stato/dominio (HTTP 409).
 *
 * <p>Esempi: duplicati, operazioni non compatibili con lo stato corrente.</p>
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
