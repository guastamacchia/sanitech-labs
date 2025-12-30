package it.sanitech.payments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Proprietà CORS del microservizio.
 *
 * <p>
 * Tutte le configurazioni arrivano da {@code application.yml} (prefisso {@code sanitech.cors}).
 * In ambienti reali è raccomandato usare origini esplicite e {@code allow-credentials=false}.
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sanitech.cors")
public class CorsProperties {

    private List<String> pathPatterns = new ArrayList<>(List.of("/api/**"));
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Request-Id"));
    private List<String> exposedHeaders = new ArrayList<>(List.of("Location", "X-Request-Id", "X-Total-Count"));
    private boolean allowCredentials = false;
    private long maxAge = 3600;
}
