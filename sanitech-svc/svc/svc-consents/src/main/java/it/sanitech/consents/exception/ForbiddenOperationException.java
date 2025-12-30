package it.sanitech.consents.exception;

/**
 * Eccezione applicativa per operazioni non consentite (HTTP 403).
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
