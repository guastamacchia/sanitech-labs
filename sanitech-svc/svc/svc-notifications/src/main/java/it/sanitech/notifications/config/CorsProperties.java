package it.sanitech.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS del microservizio.
 *
 * <p>
 * Vengono caricate da {@code application.yml} sotto il prefisso {@code sanitech.cors}.
 * L'obiettivo è avere una configurazione centralizzata e modificabile senza ricompilare.
 * </p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "sanitech.cors")
public class CorsProperties {

    /**
     * Pattern di path su cui applicare il filtro CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = new ArrayList<>(List.of("/api/**"));

    /**
     * Origini consentite (es. domini frontend). Evitare wildcard in produzione.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Metodi HTTP permessi.
     */
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    /**
     * Header permessi in richiesta.
     */
    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "Accept"));

    /**
     * Header esposti al browser.
     */
    private List<String> exposedHeaders = new ArrayList<>(List.of("Location"));

    /**
     * Se true, consente l'invio di cookie/authorization cross-site.
     * Nota: non compatibile con origini wildcard.
     */
    private boolean allowCredentials = false;

    /**
     * Durata cache del preflight (secondi).
     */
    private long maxAge = 3600;
}
