package it.sanitech.commons.autoconfigure;

import it.sanitech.commons.autoconfigure.properties.CorsProperties;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configurazione CORS condivisa tra i microservizi Sanitech.
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnClass(CorsFilter.class)
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = AppConstants.ConfigKeys.Cors.PREFIX, name = "enabled", havingValue = "true")
public class CorsAutoConfiguration {

    private final CorsProperties properties;

    /**
     * Validazione della configurazione CORS.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("CORS: avvio validazione configurazione (properties già normalizzate).");

        List<String> origins = Optional.ofNullable(properties.getAllowedOrigins()).orElse(List.of());
        List<String> methods = Optional.ofNullable(properties.getAllowedMethods()).orElse(List.of());
        List<String> headers = Optional.ofNullable(properties.getAllowedHeaders()).orElse(List.of());
        List<String> paths = Optional.ofNullable(properties.getPathPatterns()).orElse(List.of());

        boolean hasWildcardOrigin = origins.contains("*");

        if (properties.isAllowCredentials() && hasWildcardOrigin) {
            log.error("CORS: configurazione non valida. allowCredentials=true non è compatibile con allowedOrigins=['*'].");
            log.error("CORS: correzione richiesta: impostare origini esplicite oppure usare allowedOriginPatterns (supporto nativo Spring CORS).");
            throw new IllegalStateException("Configurazione CORS non valida: allowCredentials=true con allowedOrigins='*'.");
        }

        if (paths.isEmpty()) {
            log.warn("CORS: nessun pathPatterns configurato. La policy CORS non verrà applicata a nessun endpoint.");
        } else {
            log.debug("CORS: numero di pathPatterns configurati: {}", paths.size());
        }

        if (methods.isEmpty()) {
            log.warn("CORS: allowedMethods è vuoto. Le richieste cross-site potrebbero essere bloccate (preflight/OPTIONS).");
        }
        if (headers.isEmpty()) {
            log.warn("CORS: allowedHeaders è vuoto. Header custom inviati dal client potrebbero causare fallimento del preflight.");
        }

        log.debug("CORS: validazione completata con successo.");
    }

    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter() {
        log.debug("CORS: creazione CorsFilter e registrazione configurazioni per i path.");

        CorsConfiguration base = buildCorsConfiguration();

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();

        List<String> paths = properties.getPathPatterns();
        if (Objects.isNull(paths) || paths.isEmpty()) {
            log.debug("CORS: nessun path da registrare. CorsFilter creato senza configurazioni associate.");
            return new CorsFilter(src);
        }

        for (String path : paths) {
            log.debug("CORS: registrazione policy sul path pattern '{}'.", path);
            src.registerCorsConfiguration(path, new CorsConfiguration(base));
        }

        log.debug("CORS: registrazione completata. Path registrati: {}", paths.size());
        log.debug("CORS: CorsFilter inizializzato correttamente.");
        return new CorsFilter(src);
    }

    /**
     * Costruisce la {@link CorsConfiguration} a partire dalle properties (già normalizzate).
     */
    private CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(properties.getAllowedOrigins());
        cfg.setAllowedMethods(properties.getAllowedMethods());
        cfg.setAllowedHeaders(properties.getAllowedHeaders());
        cfg.setExposedHeaders(properties.getExposedHeaders());
        cfg.setAllowCredentials(properties.isAllowCredentials());
        cfg.setMaxAge(properties.getMaxAge());

        log.debug("CORS: CorsConfiguration costruita.");

        return cfg;
    }
}
