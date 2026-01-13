package it.sanitech.commons.autoconfigure;

import it.sanitech.commons.autoconfigure.properties.CorsProperties;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configurazione CORS condivisa tra i microservizi Sanitech.
 *
 * <p>
 * Questa classe si occupa di:
 * </p>
 * <ul>
 *   <li>validare la coerenza della configurazione CORS a startup</li>
 *   <li>creare e registrare un {@link CorsFilter} sui path configurati</li>
 * </ul>
 *
 * <p>
 * La normalizzazione dei valori (trim, rimozione null/vuoti, deduplica, normalizzazione path)
 * è demandata a {@link CorsProperties}.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
@ConditionalOnProperty(prefix = AppConstants.ConfigKeys.Cors.PREFIX, name = "enabled", havingValue = "true")
public class CorsAutoConfiguration {

    private final CorsProperties props;

    /**
     * Validazione della configurazione CORS.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("CORS: avvio validazione configurazione (properties già normalizzate).");

        List<String> origins = Optional.ofNullable(props.getAllowedOrigins()).orElse(List.of());
        List<String> methods = Optional.ofNullable(props.getAllowedMethods()).orElse(List.of());
        List<String> headers = Optional.ofNullable(props.getAllowedHeaders()).orElse(List.of());
        List<String> paths = Optional.ofNullable(props.getPathPatterns()).orElse(List.of());

        boolean hasWildcardOrigin = origins.contains("*");

        if (props.isAllowCredentials() && hasWildcardOrigin) {
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
    public CorsFilter corsFilter() {
        log.debug("CORS: creazione CorsFilter e registrazione configurazioni per i path.");

        CorsConfiguration base = buildCorsConfiguration();

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();

        List<String> paths = props.getPathPatterns();
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

        cfg.setAllowedOrigins(props.getAllowedOrigins());
        cfg.setAllowedMethods(props.getAllowedMethods());
        cfg.setAllowedHeaders(props.getAllowedHeaders());
        cfg.setExposedHeaders(props.getExposedHeaders());
        cfg.setAllowCredentials(props.isAllowCredentials());
        cfg.setMaxAge(props.getMaxAge());

        log.debug("CORS: CorsConfiguration costruita.");

        return cfg;
    }
}
