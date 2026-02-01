package it.sanitech.docs.utilities;

import it.sanitech.commons.security.JwtClaimExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per estrarre informazioni dal token JWT.
 *
 * <p>
 * Delega alla utility centralizzata {@link JwtClaimExtractor} per l'estrazione dei claim.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthUtils {

    public static JwtAuthenticationToken requireJwt(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt;
        }
        throw new AccessDeniedException("Autenticazione non valida.");
    }

    /**
     * Estrae l'id paziente dal claim {@code pid} (se presente).
     */
    public static Optional<Long> patientId(Authentication auth) {
        requireJwt(auth);
        return JwtClaimExtractor.patientId(auth);
    }
}
