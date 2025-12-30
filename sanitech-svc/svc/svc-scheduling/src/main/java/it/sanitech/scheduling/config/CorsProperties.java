package it.sanitech.scheduling.config;

import it.sanitech.scheduling.utilities.AppConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS del microservizio (namespace {@code sanitech.cors}).
 *
 * <p>
 * La configurazione viene letta da {@code application.yml} e consente di
 * controllare in modo dichiarativo origini, metodi e header ammessi.
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Cors.PREFIX)
public class CorsProperties {

    /**
     * Pattern di path su cui applicare la policy CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = new ArrayList<>();

    /**
     * Lista di origini autorizzate (es. {@code https://app.sanitech.it}).
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Metodi HTTP consentiti nelle richieste cross-site.
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * Header ammessi nella richiesta cross-site.
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * Header esposti al client (leggibili dal browser).
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Se true, abilita invio credenziali (cookie/Authorization) in richieste cross-site.
     */
    private boolean allowCredentials = AppConstants.ConfigDefaultValue.Cors.ALLOW_CREDENTIALS;

    /**
     * Cache del preflight (OPTIONS) espressa in secondi.
     */
    private long maxAge = AppConstants.ConfigDefaultValue.Cors.MAX_AGE_SECONDS;
}
