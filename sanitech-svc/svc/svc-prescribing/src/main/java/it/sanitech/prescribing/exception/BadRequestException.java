package it.sanitech.prescribing.exception;

/**
 * Eccezione applicativa per richieste non valide (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Crea un errore 400 per claim mancante nel JWT (configurazione/token non coerente).
     */
    public static BadRequestException missingClaim(String claimName) {
        return new BadRequestException("Claim JWT mancante: " + claimName);
    }
}
