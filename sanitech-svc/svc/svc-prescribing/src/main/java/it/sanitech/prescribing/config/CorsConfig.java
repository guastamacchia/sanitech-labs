package it.sanitech.prescribing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configurazione CORS centralizzata per il microservizio.
 *
 * <p>
 * Applica le regole configurate via {@link CorsProperties} ai path specificati.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
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
