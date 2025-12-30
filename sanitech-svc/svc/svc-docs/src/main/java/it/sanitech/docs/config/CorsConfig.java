package it.sanitech.docs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configurazione CORS centralizzata.
 *
 * <p>
 * Nota: la scelta delle origini abilitate è demandata a {@link CorsProperties}
 * per evitare hard-coding e favorire il deploy multi-ambiente.
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
        cfg.setAllowedOriginPatterns(props.getAllowedOrigins());
        cfg.setAllowedMethods(props.getAllowedMethods());
        cfg.setAllowedHeaders(props.getAllowedHeaders());
        cfg.setExposedHeaders(props.getExposedHeaders());
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        for (String pattern : props.getPathPatterns()) {
            src.registerCorsConfiguration(pattern, cfg);
        }
        return new CorsFilter(src);
    }
}
