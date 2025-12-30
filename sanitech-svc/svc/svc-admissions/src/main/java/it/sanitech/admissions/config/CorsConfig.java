package it.sanitech.admissions.config;

import it.sanitech.admissions.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configurazione CORS.
 *
 * <p>
 * Applica regole CORS configurabili da {@code application.yml} tramite {@link CorsProperties}.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Allowed origins: se sono presenti wildcard/pattern, usiamo allowedOriginPatterns.
        List<String> origins = props.getAllowedOrigins();
        boolean hasWildcards = origins.stream().anyMatch(o -> o.contains("*"));

        if (props.isAllowCredentials() && hasWildcards) {
            // Sicurezza: con allow-credentials=true non è consentito usare wildcard per le origini.
            throw new IllegalStateException(
                    "Configurazione CORS non sicura: " + AppConstants.ConfigKeys.Cors.ALLOW_CREDENTIALS
                            + "=true richiede origini esplicite (no wildcard) in "
                            + AppConstants.ConfigKeys.Cors.ALLOWED_ORIGINS
            );
        }

        if (hasWildcards) {
            cfg.setAllowedOriginPatterns(origins);
        } else {
            cfg.setAllowedOrigins(origins);
        }

        cfg.setAllowedMethods(props.getAllowedMethods());
        cfg.setAllowedHeaders(props.getAllowedHeaders());
        cfg.setExposedHeaders(props.getExposedHeaders());
        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        for (String path : props.getPathPatterns()) {
            src.registerCorsConfiguration(path, cfg);
        }

        log.info("CORS attivo su path {} (allowCredentials={})", props.getPathPatterns(), props.isAllowCredentials());
        return new CorsFilter(src);
    }
}
