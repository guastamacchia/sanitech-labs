package it.sanitech.payments.security;

import it.sanitech.payments.utilities.AppConstants;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per estrarre claim applicativi dal JWT (es. {@code pid}).
 */
@UtilityClass
public class AuthClaims {

    public static Optional<Long> patientId(Authentication auth) {
        return extractLongClaim(auth, AppConstants.Security.CLAIM_PATIENT_ID);
    }

    private static Optional<Long> extractLongClaim(Authentication auth, String claim) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        Object raw = jwtAuth.getToken().getClaims().get(claim);
        if (raw == null) return Optional.empty();

        if (raw instanceof Number n) {
            return Optional.of(n.longValue());
        }
        if (raw instanceof String s) {
            try {
                return Optional.of(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
