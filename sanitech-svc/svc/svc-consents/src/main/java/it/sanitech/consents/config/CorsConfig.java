package it.sanitech.consents.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configurazione CORS basata su property.
 * <p>
 * Nota: CORS è rilevante solo per chiamate browser cross-site. Per chiamate server-to-server
 * (es. tra microservizi) non è normalmente coinvolto.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties props;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOriginPatterns(fallback(props.getAllowedOrigins(), List.of("*")));
        cfg.setAllowedMethods(fallback(props.getAllowedMethods(), List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        cfg.setAllowedHeaders(fallback(props.getAllowedHeaders(), List.of("*")));
        cfg.setExposedHeaders(props.getExposedHeaders());

        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        for (String pattern : fallback(props.getPathPatterns(), List.of("/**"))) {
            src.registerCorsConfiguration(pattern, cfg);
        }
        return new CorsFilter(src);
    }

    private static List<String> fallback(List<String> value, List<String> defaultValue) {
        return CollectionUtils.isEmpty(value) ? defaultValue : value;
    }
}
