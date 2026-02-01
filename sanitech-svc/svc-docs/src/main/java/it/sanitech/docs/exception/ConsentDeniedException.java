package it.sanitech.docs.exception;

/**
 * Eccezione applicativa lanciata quando un medico tenta di accedere ai dati/documenti
 * di un paziente senza un consenso valido.
 */
public class ConsentDeniedException extends RuntimeException {

    private ConsentDeniedException(String message) {
        super(message);
    }

    public static ConsentDeniedException forPatient(Long patientId) {
        return new ConsentDeniedException("Consenso non presente o non valido per il paziente con id " + patientId + ".");
    }
}
