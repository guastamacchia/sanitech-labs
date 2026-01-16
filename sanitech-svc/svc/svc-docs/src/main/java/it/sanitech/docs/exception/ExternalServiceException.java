package it.sanitech.docs.exception;

/**
 * Eccezione applicativa per segnalare problemi temporanei su integrazioni esterne
 * (es. svc-consents non raggiungibile).
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
