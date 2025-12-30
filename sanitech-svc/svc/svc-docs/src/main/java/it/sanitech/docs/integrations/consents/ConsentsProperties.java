package it.sanitech.docs.integrations.consents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà di integrazione verso {@code svc-consents}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sanitech.consents")
public class ConsentsProperties {

    /** Base URL del servizio consensi (es. {@code http://svc-consents:8085}). */
    private String baseUrl = "http://localhost:8085";
}
