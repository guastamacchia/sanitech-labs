package it.sanitech.consents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS applicative (prefix: {@code sanitech.cors}).
 * <p>
 * Sono lette da {@code application.yml} così da poter variare tra ambienti
 * senza ricompilare il servizio.
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "sanitech.cors")
public class CorsProperties {

    private List<String> pathPatterns = new ArrayList<>();
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();

    private boolean allowCredentials = false;
    private long maxAge = 3600;
}
