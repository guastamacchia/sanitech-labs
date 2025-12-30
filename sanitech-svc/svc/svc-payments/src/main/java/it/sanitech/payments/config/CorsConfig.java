package it.sanitech.payments.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configurazione CORS guidata da properties.
 *
 * <p>
 * In produzione è generalmente preferibile gestire CORS a livello di Gateway.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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
        return src;
    }
}
