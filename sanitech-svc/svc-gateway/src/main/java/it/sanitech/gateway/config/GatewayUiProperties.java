package it.sanitech.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà UI del gateway (es. redirect della root verso Swagger).
 */
@Data
@ConfigurationProperties(prefix = "sanitech.gateway.ui")
public class GatewayUiProperties {

    /**
     * Se true, {@code GET /} effettua redirect 302 verso {@code /swagger.html}.
     * Utile in staging/ambiente interno.
     */
    private boolean redirectRootToSwagger = false;
}
