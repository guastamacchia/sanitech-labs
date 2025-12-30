package it.sanitech.prescribing.utilities;

import it.sanitech.prescribing.exception.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per leggere in modo consistente le informazioni dal {@link Authentication}
 * (JWT validato dal Resource Server).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Estrae il token bearer (stringa JWT) dall'Authentication corrente, se disponibile.
     * <p>
     * Utile per propagare il token verso servizi downstream (token relay).
     * </p>
     */
    public static Optional<String> bearerToken(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken().getTokenValue());
        }
        return Optional.empty();
    }

    /**
     * Estrae una claim dal JWT e prova a convertirla in {@link Long}.
     */
    public static Optional<Long> longClaim(Authentication auth, String claimName) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        Object value = jwtAuth.getToken().getClaim(claimName);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number n) {
            return Optional.of(n.longValue());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Optional.of(Long.parseLong(s));
            } catch (NumberFormatException ignore) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * @return identificativo del paziente (claim {@code pid}) oppure eccezione 400 se non presente.
     */
    public static Long requirePatientId(Authentication auth) {
        return longClaim(auth, AppConstants.JwtClaim.PATIENT_ID)
                .orElseThrow(() -> BadRequestException.missingClaim(AppConstants.JwtClaim.PATIENT_ID));
    }

    /**
     * @return identificativo del medico (claim {@code did}) oppure eccezione 400 se non presente.
     */
    public static Long requireDoctorId(Authentication auth) {
        return longClaim(auth, AppConstants.JwtClaim.DOCTOR_ID)
                .orElseThrow(() -> BadRequestException.missingClaim(AppConstants.JwtClaim.DOCTOR_ID));
    }
}
