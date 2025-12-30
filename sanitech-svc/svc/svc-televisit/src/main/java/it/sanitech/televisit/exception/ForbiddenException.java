package it.sanitech.televisit.exception;

/**
 * Eccezione applicativa per operazioni non consentite (HTTP 403).
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public static ForbiddenException of(String message) {
        return new ForbiddenException(message);
    }
}
