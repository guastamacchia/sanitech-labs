package it.sanitech.prescribing.config;

import it.sanitech.prescribing.utilities.AppConstants;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Property binder per la configurazione CORS del microservizio.
 *
 * <p>
 * Le proprietà vengono lette da {@code application.yml} sotto il prefisso {@code sanitech.cors}.
 * Questo approccio evita valori cablati nel codice e rende la configurazione adattabile per ambiente.
 * </p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Cors.PREFIX)
public class CorsProperties {

    /**
     * Pattern di path su cui applicare il filtro CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = List.of("/api/**");

    /**
     * Origini consentite (domini espliciti consigliati quando {@code allowCredentials=true}).
     */
    private List<String> allowedOrigins = List.of();

    /**
     * Metodi HTTP consentiti dal browser in CORS.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * Header ammessi nella richiesta CORS.
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Request-Id");

    /**
     * Header che il client è autorizzato a leggere dalla risposta.
     */
    private List<String> exposedHeaders = List.of("Location", "Content-Disposition", "X-Request-Id", "X-Total-Count");

    /**
     * Se true consente al browser di inviare cookie/authorization cross-site.
     * <p><b>Nota:</b> non è compatibile con origini wildcard (es. "*").</p>
     */
    private boolean allowCredentials = false;

    /**
     * Cache del preflight (OPTIONS) in secondi.
     */
    @Min(0)
    private long maxAge = 3600;
}
