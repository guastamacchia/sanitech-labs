package it.sanitech.scheduling.security;

import it.sanitech.scheduling.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility per leggere claim JWT in modo sicuro.
 *
 * <p>
 * L'implementazione è volutamente difensiva: se il token non è JWT oppure il claim
 * non è presente/non parseabile, viene restituito {@link Optional#empty()} (oppure
 * viene sollevata {@link IllegalArgumentException} nei metodi "required").
 * </p>
 */
public final class JwtClaimUtils {

    private static final String CLAIM_PATIENT_ID = AppConstants.JwtClaims.PATIENT_ID;
    private static final String CLAIM_DOCTOR_ID = AppConstants.JwtClaims.DOCTOR_ID;

    private JwtClaimUtils() { }

    public static Optional<String> getStringClaim(Authentication auth, String claimName) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object v = jwtAuth.getToken().getClaims().get(claimName);
            if (v == null) {
                v = switch (claimName) {
                    case CLAIM_PATIENT_ID -> firstPresent(jwtAuth, "patientId", "patient_id");
                    case CLAIM_DOCTOR_ID -> firstPresent(jwtAuth, "doctorId", "doctor_id");
                    default -> null;
                };
            }
            return v == null ? Optional.empty() : Optional.of(String.valueOf(v));
        }
        return Optional.empty();
    }

    public static Optional<Long> getLongClaim(Authentication auth, String claimName) {
        return getStringClaim(auth, claimName).flatMap(JwtClaimUtils::parseLong);
    }

    public static Long requireLongClaim(Authentication auth, String claimName) {
        return getLongClaim(auth, claimName)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ErrorMessage.MSG_JWT_CLAIM_MISSING_OR_INVALID_PREFIX + claimName));
    }

    private static Optional<Long> parseLong(String v) {
        try {
            return Optional.of(Long.parseLong(v));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Object firstPresent(JwtAuthenticationToken jwtAuth, String... aliases) {
        for (String alias : aliases) {
            Object value = jwtAuth.getToken().getClaims().get(alias);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
