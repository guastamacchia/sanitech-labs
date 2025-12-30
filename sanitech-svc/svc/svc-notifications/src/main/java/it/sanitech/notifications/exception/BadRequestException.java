package it.sanitech.notifications.exception;

/**
 * Eccezione applicativa per segnalare una richiesta non valida (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
