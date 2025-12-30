package it.sanitech.televisit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configurazione del filtro CORS.
 *
 * <p>Le regole sono governate da {@link CorsProperties} (file {@code application.yml}).</p>
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        cfg.setAllowedOriginPatterns(props.getAllowedOrigins());
        cfg.setAllowedMethods(props.getAllowedMethods());
        cfg.setAllowedHeaders(props.getAllowedHeaders());
        cfg.setExposedHeaders(props.getExposedHeaders());

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        for (String pattern : props.getPathPatterns()) {
            src.registerCorsConfiguration(pattern, cfg);
        }
        return new CorsFilter(src);
    }
}
