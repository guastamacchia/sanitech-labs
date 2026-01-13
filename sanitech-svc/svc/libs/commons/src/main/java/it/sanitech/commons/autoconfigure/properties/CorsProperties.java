package it.sanitech.commons.autoconfigure.properties;

import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Proprietà CORS comuni ai microservizi (namespace {@code sanitech.cors}).
 *
 * <p>
 * Questa classe rappresenta la configurazione dichiarativa e applica una normalizzazione
 * dei valori letti da YAML per garantire coerenza e ridurre errori di configurazione
 * (spazi, null, duplicati, casing dei metodi HTTP).
 * </p>
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Cors.PREFIX)
public class CorsProperties {

    /**
     * Abilita la configurazione CORS condivisa.
     */
    private boolean enabled = false;

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
     *
     * <p>
     * In normalizzazione vengono convertiti in uppercase (es. "get" -> "GET") per uniformità.
     * </p>
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
    private boolean allowCredentials = AppConstants.ConfigDefaultValue.Cors.ALLOW_CREDENTIALS;

    /**
     * Cache del preflight (OPTIONS) espressa in secondi.
     */
    private long maxAge = AppConstants.ConfigDefaultValue.Cors.MAX_AGE_SECONDS;

    /**
     * Normalizzazione delle proprietà lette da YAML.
     *
     * <p>
     * Scopo:
     * - rimuovere valori null o vuoti
     * - applicare trim
     * - eliminare duplicati (preservando l'ordine)
     * - normalizzare i path (prefisso '/')
     * - normalizzare i metodi HTTP (uppercase)
     * </p>
     */
    @PostConstruct
    void normalize() {
        log.debug("CORS properties: avvio normalizzazione valori letti da configurazione.");

        this.pathPatterns = normalizePathPatterns(this.pathPatterns);
        this.allowedOrigins = normalizeList(this.allowedOrigins);
        this.allowedMethods = normalizeMethods(this.allowedMethods);
        this.allowedHeaders = normalizeList(this.allowedHeaders);
        this.exposedHeaders = normalizeList(this.exposedHeaders);

        log.debug("CORS properties: normalizzazione completata.");
        log.debug("CORS properties: enabled={}", enabled);
        log.debug("CORS properties: pathPatterns={}", pathPatterns);
        log.debug("CORS properties: allowedOrigins={}", allowedOrigins);
        log.debug("CORS properties: allowedMethods={}", allowedMethods);
        log.debug("CORS properties: allowedHeaders={}", allowedHeaders);
        log.debug("CORS properties: exposedHeaders={}", exposedHeaders);
        log.debug("CORS properties: allowCredentials={}", allowCredentials);
        log.debug("CORS properties: maxAge (secondi)={}", maxAge);
    }

    /**
     * Normalizza una lista generica: trim, rimozione null/vuoti, deduplica preservando ordine.
     */
    private static List<String> normalizeList(List<String> raw) {
        return normalizeInternal(raw, null, null);
    }

    /**
     * Normalizza i metodi HTTP: trim, uppercase, deduplica preservando ordine.
     */
    private static List<String> normalizeMethods(List<String> raw) {
        return normalizeInternal(raw, null, s -> s.toUpperCase(Locale.ROOT));
    }

    /**
     * Normalizza i path pattern:
     * - per singole voci vuote/non valide applica fallback "/**"
     * - assicura prefisso '/'
     * - trim, rimozione vuoti, deduplica preservando ordine
     *
     * <p>
     * Nota: se l'intera lista è vuota/null, ritorna List.of() (nessun path registrato).
     * </p>
     */
    private static List<String> normalizePathPatterns(List<String> raw) {
        return normalizeInternal(raw, CorsProperties::normalizePathPreMap, null);
    }

    /**
     * Trasformazione "a monte" specifica per i path:
     * - se non ha testo -> "/**"
     * - se manca '/' all'inizio, lo aggiunge
     *
     * <p>
     * Viene invocata prima del filtro hasText e della deduplica.
     * </p>
     */
    private static String normalizePathPreMap(String path) {
        if (!StringUtils.hasText(path)) {
            return "/**";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     * Motore unico di normalizzazione per liste di stringhe.
     *
     * <p>
     * Pipeline:
     * 1) null/empty -> List.of()
     * 2) preMap opzionale (trasformazioni "a monte" specifiche del dominio)
     * 4) trim
     * 5) filter hasText
     * 6) postMap opzionale (trasformazioni "a valle": uppercase, ecc.)
     * 7) distinct (preserva ordine nello stream)
     * </p>
     *
     * @param raw     lista di input
     * @param preMap  trasformazione applicata prima di trim/filter (può gestire fallback)
     * @param postMap trasformazione applicata dopo trim/filter (finale)
     */
    private static List<String> normalizeInternal(List<String> raw,
                                                  UnaryOperator<String> preMap,
                                                  UnaryOperator<String> postMap) {

        if (Objects.isNull(raw) || raw.isEmpty()) return List.of();

        return raw.stream()
                // 1) preMap applicata anche a null
                .map(s -> Objects.nonNull(preMap) ? preMap.apply(s) : s)

                // 2) trim centralizzato
                .map(s -> Objects.nonNull(s) ? s.trim() : null)

                // 3) filtro logico: solo stringhe significative
                .filter(StringUtils::hasText)

                // 4) postMap applicata a valle dei filtri effettuati (uppercase, ecc.)
                .map(s -> Objects.nonNull(postMap) ? postMap.apply(s) : s)

                // 5) deduplica preservando ordine
                .distinct()
                .collect(Collectors.toList());
    }
}
