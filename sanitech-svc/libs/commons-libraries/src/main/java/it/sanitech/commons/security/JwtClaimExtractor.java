package it.sanitech.commons.security;

import it.sanitech.commons.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

/**
 * Utility centralizzata per l'estrazione dei claim custom dal JWT.
 *
 * <p>
 * Questa classe fornisce metodi sicuri per estrarre i claim applicativi
 * (es. {@code pid}, {@code did}) dal token JWT validato dal Resource Server.
 * </p>
 *
 * <p>
 * L'implementazione è difensiva: se il token non è JWT oppure il claim
 * non è presente/non parseabile, viene restituito {@link Optional#empty()}.
 * </p>
 */
public final class JwtClaimExtractor {

    private JwtClaimExtractor() {
    }

    /**
     * Estrae l'identificativo del paziente dal claim {@code pid}.
     *
     * @param auth l'oggetto Authentication corrente
     * @return Optional contenente il patient ID, oppure empty se non presente/non valido
     */
    public static Optional<Long> patientId(Authentication auth) {
        return extractLongClaim(auth, AppConstants.Security.CLAIM_PATIENT_ID);
    }

    /**
     * Estrae l'identificativo del medico dal claim {@code did}.
     *
     * @param auth l'oggetto Authentication corrente
     * @return Optional contenente il doctor ID, oppure empty se non presente/non valido
     */
    public static Optional<Long> doctorId(Authentication auth) {
        return extractLongClaim(auth, AppConstants.Security.CLAIM_DOCTOR_ID);
    }

    /**
     * Estrae l'identificativo del paziente dal JWT, lanciando eccezione se mancante.
     *
     * @param auth l'oggetto Authentication corrente
     * @return il patient ID
     * @throws IllegalArgumentException se il claim non è presente o non valido
     */
    public static Long requirePatientId(Authentication auth) {
        return patientId(auth)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Claim JWT mancante o non valido: " + AppConstants.Security.CLAIM_PATIENT_ID));
    }

    /**
     * Estrae l'identificativo del medico dal JWT, lanciando eccezione se mancante.
     *
     * @param auth l'oggetto Authentication corrente
     * @return il doctor ID
     * @throws IllegalArgumentException se il claim non è presente o non valido
     */
    public static Long requireDoctorId(Authentication auth) {
        return doctorId(auth)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Claim JWT mancante o non valido: " + AppConstants.Security.CLAIM_DOCTOR_ID));
    }

    /**
     * Estrae il token bearer (stringa JWT) dall'Authentication corrente.
     *
     * <p>
     * Utile per propagare il token verso servizi downstream (token relay).
     * </p>
     *
     * @param auth l'oggetto Authentication corrente
     * @return Optional contenente il token value, oppure empty se non disponibile
     */
    public static Optional<String> bearerToken(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken().getTokenValue());
        }
        return Optional.empty();
    }

    /**
     * Estrae una claim dal JWT e prova a convertirla in {@link Long}.
     *
     * @param auth      l'oggetto Authentication corrente
     * @param claimName il nome del claim da estrarre
     * @return Optional contenente il valore Long, oppure empty se non presente/non valido
     */
    public static Optional<Long> extractLongClaim(Authentication auth, String claimName) {
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
                return Optional.of(Long.parseLong(s.trim()));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Estrae una claim dal JWT come stringa.
     *
     * @param auth      l'oggetto Authentication corrente
     * @param claimName il nome del claim da estrarre
     * @return Optional contenente il valore stringa, oppure empty se non presente
     */
    public static Optional<String> extractStringClaim(Authentication auth, String claimName) {
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.empty();
        }
        Object value = jwtAuth.getToken().getClaim(claimName);
        if (value == null) {
            return Optional.empty();
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() ? Optional.empty() : Optional.of(str);
    }
}
