package it.sanitech.payments.exception;

import it.sanitech.payments.utilities.AppConstants;

/**
 * Eccezione applicativa: webhook non autorizzato (secret errato o mancante).
 */
public class WebhookUnauthorizedException extends RuntimeException {

    public WebhookUnauthorizedException(String message) {
        super(message);
    }

    public static WebhookUnauthorizedException standard() {
        return new WebhookUnauthorizedException(AppConstants.ErrorMessage.MSG_WEBHOOK_UNAUTHORIZED);
    }
}
