package it.sanitech.gateway.openapi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà HTTP per le chiamate del gateway verso gli endpoint OpenAPI dei servizi.
 *
 * <p>
 * Queste chiamate sono solo "internal plumbing" (non traffico utente) e hanno timeout conservativi
 * per evitare che Swagger UI o merge restino bloccati.
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "sanitech.gateway.openapi.http")
public class OpenApiHttpProperties {

    /** Timeout di connessione (ms). */
    private int connectTimeoutMs = 2000;

    /** Timeout di risposta (ms). */
    private int responseTimeoutMs = 5000;
}
