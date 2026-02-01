package it.sanitech.prescribing.security;

import it.sanitech.commons.security.JwtClaimExtractor;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Utility per leggere in modo consistente le informazioni dal {@link Authentication}
 * (JWT validato dal Resource Server).
 *
 * <p>
 * Delega alla utility centralizzata {@link JwtClaimExtractor} per l'estrazione dei claim.
 * </p>
 */
public final class JwtClaimUtils {

    private JwtClaimUtils() {
    }

    /**
     * Estrae il token bearer (stringa JWT) dall'Authentication corrente, se disponibile.
     * <p>
     * Utile per propagare il token verso servizi downstream (token relay).
     * </p>
     */
    public static Optional<String> bearerToken(Authentication auth) {
        return JwtClaimExtractor.bearerToken(auth);
    }

    /**
     * Estrae una claim dal JWT e prova a convertirla in {@link Long}.
     */
    public static Optional<Long> longClaim(Authentication auth, String claimName) {
        return JwtClaimExtractor.extractLongClaim(auth, claimName);
    }

    /**
     * @return identificativo del paziente (claim {@code pid}) oppure eccezione 400 se non presente.
     */
    public static Long requirePatientId(Authentication auth) {
        return JwtClaimExtractor.requirePatientId(auth);
    }

    /**
     * @return identificativo del medico (claim {@code did}) oppure eccezione 400 se non presente.
     */
    public static Long requireDoctorId(Authentication auth) {
        return JwtClaimExtractor.requireDoctorId(auth);
    }
}
