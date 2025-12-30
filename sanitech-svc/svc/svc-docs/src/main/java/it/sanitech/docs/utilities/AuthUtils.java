package it.sanitech.docs.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility per estrarre informazioni utili dall'oggetto {@link Authentication} (JWT).
 *
 * <p>
 * Consente di centralizzare la logica di parsing dei claim e delle authorities,
 * evitando duplicazioni nei service/controller.
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

    public static boolean hasRole(Authentication auth, String role) {
        if (auth == null) return false;
        String needed = AppConstants.Security.AUTH_ROLE_PREFIX + role;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(needed));
    }

    /**
     * Estrae l'id paziente dal claim {@code pid} (se presente).
     *
     * <p>
     * È utile per consentire ai pazienti (ROLE_PATIENT) di leggere i propri documenti
     * senza passare un {@code patientId} arbitrario in querystring.
     * </p>
     */
    public static Optional<Long> patientId(Authentication auth) {
        JwtAuthenticationToken jwt = requireJwt(auth);
        Object claim = jwt.getToken().getClaims().get(AppConstants.Security.CLAIM_PATIENT_ID);

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

    /**
     * Estrae i reparti (authority {@code DEPT_*}) come codici normalizzati.
     */
    public static Set<String> departments(Authentication auth) {
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith(AppConstants.Security.AUTH_DEPT_PREFIX))
                .map(a -> a.substring(AppConstants.Security.AUTH_DEPT_PREFIX.length()))
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
