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
 * Proprietà di sicurezza comuni ai microservizi (namespace {@code sanitech.security}).
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = SecurityProperties.PREFIX)
public class SecurityProperties {

    public static final String PREFIX = "sanitech.security";

    /**
     * Abilita la configurazione di sicurezza condivisa.
     */
    private boolean enabled = false;

    /**
     * Endpoint pubblici (es. Swagger/Actuator) ammessi senza autenticazione.
     */
    private List<String> publicEndpoints = new ArrayList<>();

    @PostConstruct
    void normalize() {
        log.debug("Security properties: avvio normalizzazione valori letti da configurazione.");

        publicEndpoints = normalizeList(publicEndpoints);

        log.debug("Security properties: enabled={}", enabled);
        log.debug("Security properties: publicEndpoints={}", publicEndpoints);
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
