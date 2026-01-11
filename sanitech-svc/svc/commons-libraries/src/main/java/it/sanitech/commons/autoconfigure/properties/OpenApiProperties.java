package it.sanitech.commons.autoconfigure.properties;

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
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = OpenApiProperties.PREFIX)
public class OpenApiProperties {

    public static final String PREFIX = "sanitech.openapi";

    /**
     * Abilita la configurazione OpenAPI condivisa.
     */
    private boolean enabled = false;

    /**
     * Nome del gruppo OpenAPI (es. directory, scheduling, payments).
     */
    private String group;

    /**
     * Package da scansionare per generare la specifica OpenAPI.
     */
    private List<String> packagesToScan = new ArrayList<>();

    /**
     * Titolo visualizzato nella documentazione.
     */
    private String title;

    /**
     * Versione API esposta.
     */
    private String version;

    @PostConstruct
    void normalize() {
        log.debug("OpenAPI properties: avvio normalizzazione valori letti da configurazione.");

        if (Objects.nonNull(group)) {
            group = group.trim();
        }
        if (Objects.nonNull(title)) {
            title = title.trim();
        }
        if (Objects.nonNull(version)) {
            version = version.trim();
        }

        packagesToScan = normalizeList(packagesToScan);

        log.debug("OpenAPI properties: enabled={}", enabled);
        log.debug("OpenAPI properties: group={}", group);
        log.debug("OpenAPI properties: packagesToScan={}", packagesToScan);
        log.debug("OpenAPI properties: title={}", title);
        log.debug("OpenAPI properties: version={}", version);
    }

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
