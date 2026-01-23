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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Proprietà OpenAPI/Springdoc (namespace {@code sanitech.openapi}).
 *
 * <p>
 * La classe rappresenta la configurazione dichiarativa e applica una normalizzazione
 * minima per ridurre errori di configurazione (spazi, null/valori vuoti, duplicati).
 * </p>
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.OpenApi.PREFIX)
public class OpenApiProperties {

    /**
     * Abilita la configurazione OpenAPI condivisa.
     */
    private boolean enabled = false;

    /**
     * Nome del gruppo OpenAPI (es. directory, scheduling, payments).
     *
     * <p>
     * Se vuoto o composto solo da spazi viene normalizzato a {@code null}.
     * </p>
     */
    private String group;

    /**
     * Package da scansionare per generare la specifica OpenAPI.
     *
     * <p>
     * Vengono rimossi elementi null/vuoti, applicato trim e deduplica (preservando l'ordine).
     * </p>
     */
    private List<String> packagesToScan = new ArrayList<>();

    /**
     * Titolo visualizzato nella documentazione.
     *
     * <p>
     * Se vuoto o composto solo da spazi viene normalizzato a {@code null}.
     * </p>
     */
    private String title;

    /**
     * Versione API esposta.
     *
     * <p>
     * Se vuoto o composto solo da spazi viene normalizzato a {@code null}.
     * </p>
     */
    private String version;

    /**
     * Normalizzazione delle proprietà lette da YAML.
     *
     * <p>
     * Scopo:
     * - trim di stringhe singole e conversione a null se vuote
     * - pulizia delle liste (null/vuoti, trim, deduplica)
     * </p>
     */
    @PostConstruct
    void normalize() {
        log.debug("OpenAPI properties: avvio normalizzazione valori letti da configurazione.");

        group = normalizeScalar(group);
        title = normalizeScalar(title);
        version = normalizeScalar(version);

        packagesToScan = normalizeList(packagesToScan);

        log.debug("OpenAPI properties: normalizzazione completata.");
        log.debug("OpenAPI properties: enabled={}", enabled);
        log.debug("OpenAPI properties: group={}", group);
        log.debug("OpenAPI properties: packagesToScan={}", packagesToScan);
        log.debug("OpenAPI properties: title={}", title);
        log.debug("OpenAPI properties: version={}", version);
    }

    /**
     * Normalizza una stringa singola:
     * - null -> null
     * - trim
     * - se dopo trim è vuota -> null
     */
    private static String normalizeScalar(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String t = value.trim();
        return StringUtils.hasText(t) ? t : null;
    }

    /**
     * Normalizza una lista:
     * - null/empty -> List.of()
     * - rimuove null
     * - trim
     * - rimuove stringhe vuote
     * - deduplica preservando ordine
     */
    private static List<String> normalizeList(List<String> raw) {
        if (Objects.isNull(raw) || raw.isEmpty()) return List.of();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
