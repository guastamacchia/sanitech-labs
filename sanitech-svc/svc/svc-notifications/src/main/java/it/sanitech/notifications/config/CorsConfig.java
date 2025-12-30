package it.sanitech.notifications.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configurazione del filtro CORS.
 *
 * <p>
 * La configurazione è guidata da {@link CorsProperties} (file YAML) e viene applicata
 * ai path indicati in {@code sanitech.cors.path-patterns}.
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

        if (props.getAllowedOrigins() == null || props.getAllowedOrigins().isEmpty()) {
            // In assenza di origini configurate, consentiamo tutte le origini (pattern)
            // con allowCredentials=false (default) per ridurre rischi in dev.
            cfg.addAllowedOriginPattern("*");
        } else {
            cfg.setAllowedOrigins(props.getAllowedOrigins());
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
        return new CorsFilter(src);
    }
}
