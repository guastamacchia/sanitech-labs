package it.sanitech.gateway.config;

import it.sanitech.gateway.utilities.AppConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà di configurazione per il CORS del gateway.
 *
 * <p>
 * Le policy CORS vanno definite con attenzione:
 * <ul>
 *   <li>in produzione usare origini esplicite;</li>
 *   <li>{@code allow-credentials=true} richiede origini non-wildcard;</li>
 *   <li>limitare metodi/header al minimo necessario.</li>
 * </ul>
 * </p>
 */
@Data
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Cors.PREFIX)
public class CorsProperties {

    /**
     * Pattern di path su cui applicare la policy CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = new ArrayList<>();

    /**
     * Origini consentite (domain allowlist). Supporta pattern (es. {@code https://*.sanitech.it}).
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Metodi HTTP ammessi (es. GET, POST).
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * Header ammessi nella richiesta CORS.
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * Header di risposta esposti al browser.
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Se true, consente l'invio di credenziali cross-site (cookie / Authorization).
     * <p>
     * Nota: con {@code allow-credentials=true} non si può usare l'origine wildcard {@code *}.
     * </p>
     */
    private boolean allowCredentials = AppConstants.ConfigDefaultValue.Cors.ALLOW_CREDENTIALS;

    /**
     * Cache del preflight (OPTIONS), in secondi.
     */
    private long maxAge = AppConstants.ConfigDefaultValue.Cors.MAX_AGE_SECONDS;
}
