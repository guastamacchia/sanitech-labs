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
 * Proprietà di sicurezza comuni ai microservizi (namespace {@code sanitech.security}).
 *
 * <p>
 * Questa classe rappresenta la configurazione dichiarativa e applica una normalizzazione
 * minima per ridurre errori di configurazione (spazi, null/valori vuoti, duplicati).
 * </p>
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Security.PREFIX)
public class SecurityProperties {

    /**
     * Abilita la configurazione di sicurezza condivisa.
     */
    private boolean enabled = false;

    /**
     * Endpoint pubblici (es. Swagger/Actuator) ammessi senza autenticazione.
     *
     * <p>
     * I valori vengono normalizzati con trim, rimozione null/vuoti e deduplica preservando l'ordine.
     * </p>
     */
    private List<String> publicEndpoints = new ArrayList<>();

    /**
     * Normalizzazione delle proprietà lette da YAML.
     *
     * <p>
     * Scopo:
     * - rimuovere valori null o vuoti
     * - applicare trim
     * - eliminare duplicati preservando l'ordine
     * </p>
     */
    @PostConstruct
    void normalize() {
        log.debug("Security properties: avvio normalizzazione valori letti da configurazione.");

        publicEndpoints = normalizeList(publicEndpoints);

        log.debug("Security properties: normalizzazione completata.");
        log.debug("Security properties: enabled={}", enabled);
        log.debug("Security properties: publicEndpoints={}", publicEndpoints);

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
