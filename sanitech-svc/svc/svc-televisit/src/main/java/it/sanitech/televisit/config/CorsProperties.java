package it.sanitech.televisit.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Proprietà CORS del microservizio.
 *
 * <p>Fonte: {@code sanitech.cors.*} in {@code application.yml}.</p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sanitech.cors")
public class CorsProperties {

    /**
     * Pattern di path su cui applicare la configurazione CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = List.of("/api/**");

    /**
     * Origini consentite (es. domini della UI). Evitare wildcard in produzione.
     */
    private List<String> allowedOrigins = List.of();

    /**
     * Metodi HTTP consentiti.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * Header consentiti nella richiesta.
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Request-Id");

    /**
     * Header esposti al browser nella risposta.
     */
    private List<String> exposedHeaders = List.of("Location", "Content-Disposition", "X-Request-Id", "X-Total-Count");

    /**
     * Se true, abilita l'invio di cookie/authorization cross-site.
     * Non è compatibile con origini wildcard.
     */
    private boolean allowCredentials = false;

    /**
     * Cache del preflight (secondi).
     */
    @Min(0)
    private long maxAge = 3600;
}
