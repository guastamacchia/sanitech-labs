package it.sanitech.payments.exception;

import it.sanitech.payments.utilities.AppConstants;

/**
 * Eccezione applicativa: accesso negato a una risorsa di pagamento.
 */
public class PaymentAccessDeniedException extends RuntimeException {

    public PaymentAccessDeniedException(String message) {
        super(message);
    }

    public static PaymentAccessDeniedException standard() {
        return new PaymentAccessDeniedException(AppConstants.ErrorMessage.MSG_ACCESS_DENIED);
    }
}
