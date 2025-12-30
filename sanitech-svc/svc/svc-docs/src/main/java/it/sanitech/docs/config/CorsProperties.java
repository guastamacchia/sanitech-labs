package it.sanitech.docs.config;

import it.sanitech.docs.utilities.AppConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Proprietà CORS del microservizio, lette da {@code application.yml} sotto {@code sanitech.cors}.
 *
 * <p>
 * Obiettivo: consentire configurazione per ambiente (local/staging/prod) senza
 * hard-coding nel codice.
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Cors.PREFIX)
public class CorsProperties {

    /** Pattern di path su cui applicare la configurazione CORS (es. {@code /api/**}). */
    private List<String> pathPatterns = List.of("/api/**");

    /** Origini abilitate (senza wildcard se {@code allowCredentials=true}). */
    private List<String> allowedOrigins = List.of("http://localhost:4200");

    /** Metodi HTTP permessi in CORS (es. GET/POST/PUT...). */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /** Header permessi in richiesta. */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Request-Id");

    /** Header esposti in risposta (leggibili dal browser). */
    private List<String> exposedHeaders = List.of("Location", "Content-Disposition", "X-Request-Id", "X-Total-Count");

    /**
     * Se {@code true}, abilita l'invio di credenziali (cookie/authorization) nelle richieste cross-site.
     * Per motivi di sicurezza, in produzione usare origini esplicite e non {@code *}.
     */
    private boolean allowCredentials = AppConstants.ConfigDefaultValues.Cors.ALLOW_CREDENTIALS;

    /** Cache del preflight (OPTIONS) in secondi. */
    private long maxAge = AppConstants.ConfigDefaultValues.Cors.MAX_AGE_SECONDS;
}
