package it.sanitech.scheduling.security;

import it.sanitech.commons.security.JwtClaimExtractor;
import it.sanitech.scheduling.utilities.AppConstants;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Utility per leggere claim JWT in modo sicuro.
 *
 * <p>
 * Delega alla utility centralizzata {@link JwtClaimExtractor} per l'estrazione dei claim.
 * </p>
 */
public final class JwtClaimUtils {

    private JwtClaimUtils() { }

    public static Optional<String> getStringClaim(Authentication auth, String claimName) {
        return JwtClaimExtractor.extractStringClaim(auth, claimName);
    }

    public static Optional<Long> getLongClaim(Authentication auth, String claimName) {
        return JwtClaimExtractor.extractLongClaim(auth, claimName);
    }

    public static Long requireLongClaim(Authentication auth, String claimName) {
        return getLongClaim(auth, claimName)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ErrorMessage.MSG_JWT_CLAIM_MISSING_OR_INVALID_PREFIX + claimName));
    }
}
