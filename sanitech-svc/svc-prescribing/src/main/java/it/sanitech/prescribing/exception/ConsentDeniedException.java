package it.sanitech.prescribing.exception;

import it.sanitech.prescribing.utilities.AppConstants;

/**
 * Eccezione funzionale: il medico non dispone del consenso necessario.
 */
public class ConsentDeniedException extends RuntimeException {

    public ConsentDeniedException(String message) {
        super(message);
    }

    public static ConsentDeniedException forPatient(Long patientId) {
        return new ConsentDeniedException(AppConstants.ErrorMessage.MSG_CONSENT_REQUIRED + " (patientId=" + patientId + ")");
    }
}
