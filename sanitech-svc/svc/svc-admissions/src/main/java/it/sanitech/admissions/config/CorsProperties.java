package it.sanitech.admissions.config;

import it.sanitech.admissions.utilities.AppConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS del microservizio, mappate dal namespace {@code sanitech.cors}.
 *
 * <p>
 * Sono volutamente configurabili via YAML per evitare valori hard-coded in codice
 * e per permettere differenze tra ambienti (local/staging/prod).
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "sanitech.cors")
public class CorsProperties {

    /**
     * Pattern di path su cui applicare il filtro CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = List.of("/api/**");

    /**
     * Origini consentite (es. {@code https://app.sanitech.it}).
     * <p>Se l'origine contiene {@code *} verrà trattata come pattern.</p>
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Metodi HTTP consentiti nelle richieste CORS.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * Header consentiti nelle richieste CORS.
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * Header esposti al client (browser) in lettura.
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Se true, consente l'invio di credenziali (cookie/Authorization) in cross-site.
     */
    private boolean allowCredentials = AppConstants.ConfigDefaultValue.Cors.ALLOW_CREDENTIALS;

    /**
     * Durata (secondi) della cache preflight (OPTIONS).
     */
    private long maxAge = AppConstants.ConfigDefaultValue.Cors.MAX_AGE_SECONDS;
}
