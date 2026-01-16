package it.sanitech.gateway.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Controller che espone OpenAPI centralizzata sul gateway.
 *
 * <p>
 * Endpoints:
 * <ul>
 *   <li>{@code GET /openapi/{service}}: proxy della specifica di un singolo servizio (whitelist);</li>
 *   <li>{@code GET /openapi/merged}: merge delle specifiche (utile per Swagger UI e client generation).</li>
 * </ul>
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/openapi")
public class OpenApiProxyController {

    private final OpenApiTargetsProperties props;
    private final OpenApiMergeService mergeService;

    @GetMapping(value = "/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> byService(@PathVariable String service) {
        if (props.getTargets() == null || !props.getTargets().containsKey(service)) {
            return Mono.error(new ResponseStatusException(NOT_FOUND, "Servizio OpenAPI non configurato: " + service));
        }

        return mergeService.serviceJson(service)
                .map(json -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json)
                );
    }

    @GetMapping(value = "/merged", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> merged() {
        return mergeService.mergedJson()
                .map(json -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json)
                );
    }
}
