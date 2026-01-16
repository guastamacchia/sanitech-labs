package it.sanitech.prescribing.integrations.consents;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configurazione per l'integrazione con {@code svc-consents}.
 *
 * <p>
 * L'obiettivo è mantenere disaccoppiata la URL del servizio consensi, rendendola
 * sovrascrivibile via environment.
 * </p>
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sanitech.integrations.consents")
public class ConsentsProperties {

    /**
     * Base URL di {@code svc-consents} (es. {@code http://svc-consents:8085}).
     */
    @NotBlank
    private String baseUrl = "http://localhost:8085";
}
