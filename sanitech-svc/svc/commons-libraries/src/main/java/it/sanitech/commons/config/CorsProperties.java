package it.sanitech.commons.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS comuni ai microservizi (namespace {@code sanitech.cors}).
 *
 * <p>
 * La configurazione viene letta da {@code application.yml} e consente di
 * controllare in modo dichiarativo origini, metodi e header ammessi.
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CorsProperties.PREFIX)
public class CorsProperties {

    public static final String PREFIX = "sanitech.cors";

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
    private boolean allowCredentials = false;

    /**
     * Cache del preflight (OPTIONS) espressa in secondi.
     */
    private long maxAge = 3600;
}
