package it.sanitech.payments.security;

import it.sanitech.payments.exception.PaymentAccessDeniedException;
import it.sanitech.payments.repositories.entities.PaymentOrder;
import it.sanitech.payments.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Guard applicativo per l'accesso alle risorse di pagamento.
 *
 * <p>
 * Regole:
 * <ul>
 *   <li>ADMIN: accesso completo</li>
 *   <li>PATIENT: accesso solo ai pagamenti con {@code patientId} uguale al claim {@code pid}</li>
 * </ul>
 * </p>
 */
@Component
public class PaymentAccessGuard {

    public void checkCanAccess(PaymentOrder order, Authentication auth) {
        if (auth == null) {
            throw PaymentAccessDeniedException.standard();
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> AppConstants.Security.ROLE_ADMIN.equals(a.getAuthority()));
        if (isAdmin) return;

        boolean isPatient = auth.getAuthorities().stream()
                .anyMatch(a -> AppConstants.Security.ROLE_PATIENT.equals(a.getAuthority()));
        if (isPatient) {
            Long pid = AuthClaims.patientId(auth).orElse(null);
            if (pid != null && pid.equals(order.getPatientId())) return;
        }

        throw PaymentAccessDeniedException.standard();
    }
}
