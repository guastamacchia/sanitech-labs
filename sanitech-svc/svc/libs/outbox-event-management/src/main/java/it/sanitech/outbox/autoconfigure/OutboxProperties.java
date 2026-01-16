package it.sanitech.outbox.autoconfigure;

import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Proprietà Outbox (namespace {@code sanitech.outbox}).
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.Outbox.PREFIX)
public class OutboxProperties {

    /**
     * Topic Kafka su cui pubblicare gli eventi outbox del microservizio.
     */
    private String topic;

    @PostConstruct
    void normalize() {
        log.debug("Outbox properties: avvio normalizzazione valori letti da configurazione.");

        topic = normalizeScalar(topic);

        log.debug("Outbox properties: normalizzazione completata.");
        log.debug("Outbox properties: topic={}", topic);
    }

    private static String normalizeScalar(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElse(null);
    }
}
