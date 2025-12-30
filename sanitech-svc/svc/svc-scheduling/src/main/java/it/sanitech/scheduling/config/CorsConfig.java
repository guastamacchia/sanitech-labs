package it.sanitech.scheduling.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configurazione CORS.
 *
 * <p>
 * La policy viene costruita leggendo {@link CorsProperties} (da YAML) e
 * applicata ai path configurati.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(props.getAllowedOrigins());
        cfg.setAllowedMethods(props.getAllowedMethods());
        cfg.setAllowedHeaders(props.getAllowedHeaders());
        cfg.setExposedHeaders(props.getExposedHeaders());
        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        // Nota di sicurezza: allowCredentials=true NON è compatibile con origini wildcard.
        if (props.isAllowCredentials() && props.getAllowedOrigins().stream().anyMatch(o -> "*".equals(o))) {
            log.warn("Configurazione CORS: allow-credentials=true non è compatibile con allowed-origins=['*'].");
        }

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        for (String path : props.getPathPatterns()) {
            src.registerCorsConfiguration(path, cfg);
        }
        return new CorsFilter(src);
    }
}
