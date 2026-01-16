package it.sanitech.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configurazione CORS (WebFlux) del gateway.
 *
 * <p>
 * Il gateway è tipicamente il punto corretto dove applicare CORS, perché rappresenta
 * l'entry-point HTTP per frontend web e client cross-origin.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // Per supportare wildcard controllati (es. https://*.sanitech.it) usiamo originPatterns.
        // In produzione preferire origini esplicite.
        if (!CollectionUtils.isEmpty(props.getAllowedOrigins())) {
            cfg.setAllowedOriginPatterns(props.getAllowedOrigins());
        }

        if (!CollectionUtils.isEmpty(props.getAllowedMethods())) {
            cfg.setAllowedMethods(props.getAllowedMethods());
        }

        if (!CollectionUtils.isEmpty(props.getAllowedHeaders())) {
            cfg.setAllowedHeaders(props.getAllowedHeaders());
        }

        if (!CollectionUtils.isEmpty(props.getExposedHeaders())) {
            cfg.setExposedHeaders(props.getExposedHeaders());
        }

        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        List<String> patterns = props.getPathPatterns();

        // Se non specificato, applichiamo CORS a tutte le route (scelta prudenziale per gateway).
        if (CollectionUtils.isEmpty(patterns)) {
            source.registerCorsConfiguration("/**", cfg);
        } else {
            for (String pattern : patterns) {
                source.registerCorsConfiguration(pattern, cfg);
            }
        }

        return new CorsWebFilter(source);
    }
}
