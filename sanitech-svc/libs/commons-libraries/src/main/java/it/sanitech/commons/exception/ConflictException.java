package it.sanitech.commons.exception;

/**
 * Eccezione applicativa che indica un conflitto di dominio (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
