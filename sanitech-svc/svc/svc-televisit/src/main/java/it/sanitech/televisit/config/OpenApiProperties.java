package it.sanitech.televisit.config;

import it.sanitech.televisit.utilities.AppConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà OpenAPI del microservizio.
 *
 * <p>Fonte: {@code sanitech.openapi.*} in {@code application.yml}.</p>
 */
@Data
@ConfigurationProperties(prefix = "sanitech.openapi")
public class OpenApiProperties {

    /**
     * Titolo mostrato in Swagger UI.
     */
    private String title = AppConstants.OpenApi.TITLE;

    /**
     * Versione API (es. v1).
     */
    private String version = AppConstants.OpenApi.VERSION;
}
