package it.sanitech.commons.autoconfigure;

import it.sanitech.commons.autoconfigure.properties.CorsProperties;
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
@ConditionalOnProperty(prefix = CorsProperties.PREFIX, name = "enabled", havingValue = "true")
public class CorsAutoConfiguration {

    private final CorsProperties props;

    /**
     * Validazione della configurazione CORS.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("CORS: avvio validazione configurazione (properties già normalizzate).");

        List<String> origins = props.getAllowedOrigins();
        List<String> methods = props.getAllowedMethods();
        List<String> headers = props.getAllowedHeaders();
        List<String> exposed = props.getExposedHeaders();
        List<String> paths = props.getPathPatterns();

        log.debug("CORS: allowCredentials={}", props.isAllowCredentials());
        log.debug("CORS: maxAge (secondi)={}", props.getMaxAge());
        log.debug("CORS: allowedOrigins={}", origins);
        log.debug("CORS: allowedMethods={}", methods);
        log.debug("CORS: allowedHeaders={}", headers);
        log.debug("CORS: exposedHeaders={}", exposed);
        log.debug("CORS: pathPatterns={}", paths);

        boolean hasWildcardOrigin = Objects.nonNull(origins) && origins.stream().anyMatch("*"::equals);

        if (props.isAllowCredentials() && hasWildcardOrigin) {
            log.error("CORS: configurazione non valida. allowCredentials=true non è compatibile con allowedOrigins=['*'].");
            log.error("CORS: correzione richiesta: impostare origini esplicite oppure migrare a allowedOriginPatterns.");
            throw new IllegalStateException("Configurazione CORS non valida: allowCredentials=true con allowedOrigins='*'.");
        }

        if (Objects.isNull(paths) || paths.isEmpty()) {
            log.warn("CORS: nessun pathPatterns configurato. La policy CORS non verrà applicata a nessun endpoint.");
        } else {
            log.debug("CORS: numero di pathPatterns configurati: {}", paths.size());
        }

        if (Objects.isNull(methods) || methods.isEmpty()) {
            log.warn("CORS: allowedMethods è vuoto. Le richieste cross-site potrebbero essere bloccate (preflight/OPTIONS).");
        }
        if (Objects.isNull(headers) || headers.isEmpty()) {
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

        log.debug("CORS: CorsConfiguration costruita con le seguenti impostazioni: " +
                  "allowedOrigins={}, allowedMethods={}, allowedHeaders={}, exposedHeaders={}, " +
                  "allowCredentials={}, maxAge={}.",
                  props.getAllowedOrigins(),
                  props.getAllowedMethods(),
                  props.getAllowedHeaders(),
                  props.getExposedHeaders(),
                  props.isAllowCredentials(),
                  props.getMaxAge()
        );

        return cfg;
    }
}
