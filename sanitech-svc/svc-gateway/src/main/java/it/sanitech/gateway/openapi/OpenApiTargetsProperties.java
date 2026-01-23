package it.sanitech.gateway.openapi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configurazione degli endpoint OpenAPI dei microservizi downstream.
 *
 * <p>
 * Il gateway espone:
 * <ul>
 *   <li>{@code GET /openapi/{service}}: proxy della specifica di un singolo servizio;</li>
 *   <li>{@code GET /openapi/merged}: merge delle specifiche (utile per Swagger UI e client generation).</li>
 * </ul>
 * </p>
 *
 * <p>
 * La proprietà {@code targets} è una whitelist: solo i servizi presenti qui possono essere richiesti,
 * prevenendo SSRF tramite il path parameter {@code service}.
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "sanitech.gateway.openapi")
public class OpenApiTargetsProperties {

    /**
     * Mappa: serviceName → URL completo dell'endpoint OpenAPI del servizio.
     * Esempio: directory → http://svc-directory:8082/v3/api-docs/directory
     */
    private Map<String, String> targets = new LinkedHashMap<>();

    /**
     * TTL cache in secondi per la specifica merged (riduce chiamate ai microservizi).
     */
    private int mergedCacheTtlSeconds = 30;
}
