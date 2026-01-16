package it.sanitech.prescribing.exception;

import it.sanitech.commons.utilities.AppConstants;

/**
 * Eccezione per errori di integrazione con servizi downstream (es. {@code svc-consents}).
 *
 * <p>
 * Tradotta in HTTP 503 dal {@link PrescribingExceptionHandler}.
 * </p>
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalServiceException(String message) {
        super(message);
    }

    public static ExternalServiceException downstream(String serviceName, Throwable cause) {
        return new ExternalServiceException("Errore chiamando " + serviceName + ".", cause);
    }

    public static ExternalServiceException unavailable(String serviceName, Throwable cause) {
        return new ExternalServiceException(AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE + " (" + serviceName + ")", cause);
    }

    public static ExternalServiceException missingBearerToken(String message) {
        return new ExternalServiceException(message);
    }
}
