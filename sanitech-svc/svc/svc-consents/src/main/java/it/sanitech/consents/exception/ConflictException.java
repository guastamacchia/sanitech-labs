package it.sanitech.consents.exception;

/**
 * Eccezione applicativa per conflitti di stato (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
