package it.sanitech.admissions.exception;

/**
 * Eccezione applicativa sollevata quando un reparto non ha posti letto disponibili.
 */
public class NoBedAvailableException extends RuntimeException {

    public NoBedAvailableException(String message) {
        super(message);
    }

    public static NoBedAvailableException forDepartment(String dept) {
        return new NoBedAvailableException("Nessun posto letto disponibile per il reparto: " + dept);
    }
}
