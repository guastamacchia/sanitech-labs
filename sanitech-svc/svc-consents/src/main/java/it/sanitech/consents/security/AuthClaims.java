package it.sanitech.consents.security;

import it.sanitech.commons.security.JwtClaimExtractor;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Utility per estrarre claim applicativi dal {@link Authentication}.
 *
 * <p>
 * Delega alla utility centralizzata {@link JwtClaimExtractor} per l'estrazione dei claim.
 * </p>
 */
public final class AuthClaims {

    private AuthClaims() {}

    public static Optional<Long> patientId(Authentication auth) {
        return JwtClaimExtractor.patientId(auth);
    }

    public static Optional<Long> doctorId(Authentication auth) {
        return JwtClaimExtractor.doctorId(auth);
    }
}
