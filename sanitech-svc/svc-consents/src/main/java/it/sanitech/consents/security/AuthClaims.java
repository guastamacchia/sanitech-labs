package it.sanitech.consents.security;

import it.sanitech.consents.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per estrarre claim applicativi dal {@link Authentication}.
 */
public final class AuthClaims {

    private AuthClaims() {}

    public static Optional<Long> patientId(Authentication auth) {
        return claimAsLong(auth, AppConstants.Security.CLAIM_PATIENT_ID);
    }

    public static Optional<Long> doctorId(Authentication auth) {
        return claimAsLong(auth, AppConstants.Security.CLAIM_DOCTOR_ID);
    }

    private static Optional<Long> claimAsLong(Authentication auth, String claimName) {
        if (!(auth instanceof JwtAuthenticationToken token)) {
            return Optional.empty();
        }
        Object v = token.getTokenAttributes().get(claimName);
        if (v == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(String.valueOf(v)));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
