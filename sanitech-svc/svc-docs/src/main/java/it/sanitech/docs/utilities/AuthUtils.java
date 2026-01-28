package it.sanitech.docs.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per estrarre informazioni dal token JWT.
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
        JwtAuthenticationToken jwt = requireJwt(auth);
        Object claim = jwt.getToken().getClaims().get(AppConstants.Security.CLAIM_PATIENT_ID);
        if (claim == null) {
            claim = jwt.getToken().getClaims().get("patientId");
        }
        if (claim == null) {
            claim = jwt.getToken().getClaims().get("patient_id");
        }

        if (claim instanceof Number n) {
            return Optional.of(n.longValue());
        }
        if (claim instanceof String s) {
            try {
                return Optional.of(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
