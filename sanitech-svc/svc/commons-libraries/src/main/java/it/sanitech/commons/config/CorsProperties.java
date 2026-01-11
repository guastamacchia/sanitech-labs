package it.sanitech.commons.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Proprietà CORS comuni ai microservizi (namespace {@code sanitech.cors}).
 *
 * <p>
 * Questa classe rappresenta esclusivamente la configurazione dichiarativa.
 * Normalizza i valori letti da YAML per garantire coerenza e ridurre errori
 * di configurazione (spazi, null, duplicati).
 * </p>
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = CorsProperties.PREFIX)
public class CorsProperties {

    public static final String PREFIX = "sanitech.cors";

    /**
     * Pattern di path su cui applicare la policy CORS (es. {@code /api/**}).
     */
    private List<String> pathPatterns = new ArrayList<>();

    /**
     * Lista di origini autorizzate (es. {@code https://app.sanitech.it}).
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Metodi HTTP consentiti nelle richieste cross-site.
     */
    private List<String> allowedMethods = new ArrayList<>();

    /**
     * Header ammessi nella richiesta cross-site.
     */
    private List<String> allowedHeaders = new ArrayList<>();

    /**
     * Header esposti al client (leggibili dal browser).
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Se true, abilita invio credenziali (cookie/Authorization) in richieste cross-site.
     */
    private boolean allowCredentials = false;

    /**
     * Cache del preflight (OPTIONS) espressa in secondi.
     */
    private long maxAge = 3600;

    /**
     * Normalizzazione delle proprietà lette da YAML.
     *
     * <p>
     * Scopo:
     * - rimuovere valori null o vuoti
     * - applicare trim
     * - eliminare duplicati
     * - normalizzare i path (prefisso '/')
     * </p>
     */
    @PostConstruct
    void normalize() {
        log.debug("CORS properties: avvio normalizzazione valori letti da configurazione.");

        this.pathPatterns = normalizePathPatterns(this.pathPatterns);
        this.allowedOrigins = normalizeList(this.allowedOrigins);
        this.allowedMethods = normalizeList(this.allowedMethods);
        this.allowedHeaders = normalizeList(this.allowedHeaders);
        this.exposedHeaders = normalizeList(this.exposedHeaders);

        log.debug("CORS properties: normalizzazione completata.");
        log.debug("CORS properties: pathPatterns={}", pathPatterns);
        log.debug("CORS properties: allowedOrigins={}", allowedOrigins);
        log.debug("CORS properties: allowedMethods={}", allowedMethods);
        log.debug("CORS properties: allowedHeaders={}", allowedHeaders);
        log.debug("CORS properties: exposedHeaders={}", exposedHeaders);
        log.debug("CORS properties: allowCredentials={}", allowCredentials);
        log.debug("CORS properties: maxAge (secondi)={}", maxAge);
    }

    /**
     * Normalizza i path pattern: rimuove null/vuoti, trim, assicura prefisso '/', deduplica.
     * Se un valore è vuoto/non valido, usa "/**" come fallback.
     */
    private static List<String> normalizePathPatterns(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        return raw.stream()
            .map(CorsProperties::normalizePath) // 1) trasforma
            .toList()
            .stream()
            .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    CorsProperties::normalizeList // 2) pulisce e deduplica
            ));
    }

    private static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/**";
        }
        String t = path.trim();
        return t.startsWith("/") ? t : "/" + t;
    }

    /**
     * Normalizza una lista: rimuove null/vuoti, fa trim, deduplica preservando l’ordine.
     */
    private static List<String> normalizeList(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
