package it.sanitech.payments.security;

import it.sanitech.commons.security.JwtClaimExtractor;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Utility per estrarre claim applicativi dal JWT (es. {@code pid}).
 *
 * <p>
 * Delega alla utility centralizzata {@link JwtClaimExtractor} per l'estrazione dei claim.
 * </p>
 */
@UtilityClass
public class AuthClaims {

    public static Optional<Long> patientId(Authentication auth) {
        return JwtClaimExtractor.patientId(auth);
    }
}
