package it.sanitech.gateway.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import it.sanitech.gateway.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servizio che scarica e unisce le specifiche OpenAPI dei microservizi downstream.
 *
 * <p>
 * Obiettivi:
 * <ul>
 *   <li>Esposizione di un'unica specifica “merged” dal gateway;</li>
 *   <li>Riduzione collisioni tra component tramite namespacing per servizio;</li>
 *   <li>Cache con TTL per evitare chiamate continue ai servizi.</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiMergeService {

    private static final String FIELD_OPENAPI = "openapi";
    private static final String FIELD_INFO = "info";
    private static final String FIELD_PATHS = "paths";
    private static final String FIELD_COMPONENTS = "components";
    private static final String FIELD_TAGS = "tags";

    private static final String FIELD_REF = "$ref";

    private static final List<String> COMPONENT_GROUPS_TO_NAMESPACE = List.of(
            "schemas",
            "responses",
            "parameters",
            "requestBodies",
            "headers"
    );

    private final WebClient openApiWebClient;
    private final ObjectMapper objectMapper;
    private final OpenApiTargetsProperties props;

    private volatile String cachedMergedJson;
    private final AtomicLong cachedAtMillis = new AtomicLong(0);

    /**
     * Restituisce la specifica OpenAPI “merged”.
     * <p>
     * Se presente in cache e non scaduta, viene restituita senza ricontattare i servizi.
     * </p>
     */
    public Mono<String> mergedJson() {
        long now = System.currentTimeMillis();
        long ttlMs = Duration.ofSeconds(Math.max(props.getMergedCacheTtlSeconds(), 1)).toMillis();

        String cached = cachedMergedJson;
        if (cached != null && (now - cachedAtMillis.get()) < ttlMs) {
            return Mono.just(cached);
        }

        return fetchAll()
                .collectList()
                .map(this::mergeSpecs)
                .map(this::toJson)
                .doOnNext(json -> {
                    cachedMergedJson = json;
                    cachedAtMillis.set(System.currentTimeMillis());
                });
    }


    /**
     * Restituisce la specifica OpenAPI di un singolo servizio (proxy).
     *
     * <p>
     * La URL viene risolta esclusivamente tramite la whitelist {@code sanitech.gateway.openapi.targets}.
     * </p>
     *
     * @param service nome logico del servizio (es. {@code directory})
     * @return JSON OpenAPI del servizio
     */
    public Mono<String> serviceJson(String service) {
        if (service == null || service.isBlank()) {
            return Mono.error(new IllegalArgumentException("Service name is blank"));
        }
        String url = props.getTargets().get(service);
        if (url == null || url.isBlank()) {
            return Mono.error(new IllegalArgumentException("Servizio OpenAPI non configurato: " + service));
        }

        return openApiWebClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Scarica tutte le specifiche OpenAPI dichiarate nella whitelist {@code targets}.
     */
    private Flux<ServiceSpec> fetchAll() {
        if (props.getTargets() == null || props.getTargets().isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(props.getTargets().entrySet())
                .flatMap(entry -> fetchOne(entry.getKey(), entry.getValue())
                        .onErrorResume(ex -> {
                            log.warn("OpenAPI fetch failed for service={} url={} (ignored): {}", entry.getKey(), entry.getValue(), ex.toString());
                            return Mono.empty();
                        })
                );
    }

    private Mono<ServiceSpec> fetchOne(String service, String url) {
        return openApiWebClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> ensureObjectNode(service, node))
                .map(spec -> namespaceComponents(service, spec))
                .map(spec -> new ServiceSpec(service, spec));
    }

    private ObjectNode ensureObjectNode(String service, JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new IllegalStateException("OpenAPI spec non valida per service=" + service);
        }
        return (ObjectNode) node;
    }

    /**
     * Applica un namespacing ai componenti per evitare collisioni tra microservizi.
     *
     * <p>
     * Esempio: {@code components.schemas.Doctor} di {@code directory} diventa
     * {@code components.schemas.directory_Doctor}, e tutti i {@code $ref} vengono riscritti.
     * </p>
     */
    private ObjectNode namespaceComponents(String service, ObjectNode spec) {
        JsonNode compsNode = spec.get(FIELD_COMPONENTS);
        if (compsNode == null || !compsNode.isObject()) {
            return spec;
        }
        ObjectNode components = (ObjectNode) compsNode;

        Map<String, String> refRewrites = new HashMap<>();

        for (String group : COMPONENT_GROUPS_TO_NAMESPACE) {
            JsonNode groupNode = components.get(group);
            if (groupNode == null || !groupNode.isObject()) {
                continue;
            }

            ObjectNode groupObj = (ObjectNode) groupNode;
            ObjectNode renamed = objectMapper.createObjectNode();

            groupObj.fieldNames().forEachRemaining(key -> {
                String newKey = service + "_" + key;
                renamed.set(newKey, groupObj.get(key));

                String oldRef = "#/components/" + group + "/" + key;
                String newRef = "#/components/" + group + "/" + newKey;
                refRewrites.put(oldRef, newRef);
            });

            components.set(group, renamed);
        }

        // Riscrive SOLO i campi "$ref" (evita sostituzioni accidentali su descrizioni/testi liberi).
        rewriteRefs(spec, refRewrites);

        return spec;
    }

    private void rewriteRefs(JsonNode node, Map<String, String> refRewrites) {
        if (node == null || refRewrites.isEmpty()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();

                if (FIELD_REF.equals(entry.getKey()) && entry.getValue() != null && entry.getValue().isTextual()) {
                    String current = entry.getValue().asText();
                    String rewritten = refRewrites.get(current);
                    if (rewritten != null) {
                        obj.put(FIELD_REF, rewritten);
                    }
                    continue;
                }

                rewriteRefs(entry.getValue(), refRewrites);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                rewriteRefs(child, refRewrites);
            }
        }
    }

    /**
     * Merge delle specifiche: unione di paths/components/tags.
     *
     * <p>
     * In caso di collisione sui path, viene mantenuta la prima definizione trovata (log warning).
     * Le collisioni sui component sono ridotte dal namespacing.
     * </p>
     */
    private ObjectNode mergeSpecs(List<ServiceSpec> specs) {
        ObjectNode merged = objectMapper.createObjectNode();

        // openapi version (prende la prima disponibile, fallback 3.0.1)
        String openapiVer = specs.stream()
                .map(s -> s.spec.path(FIELD_OPENAPI).asText(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("3.0.1");
        merged.put(FIELD_OPENAPI, openapiVer);

        // info
        ObjectNode info = objectMapper.createObjectNode();
        info.put("title", AppConstants.OpenApi.TITLE_PLATFORM);
        info.put("version", AppConstants.OpenApi.VERSION_V1);
        merged.set(FIELD_INFO, info);

        ObjectNode mergedPaths = objectMapper.createObjectNode();
        ObjectNode mergedComponents = objectMapper.createObjectNode();
        ArrayNode mergedTags = objectMapper.createArrayNode();

        merged.set(FIELD_PATHS, mergedPaths);
        merged.set(FIELD_COMPONENTS, mergedComponents);
        merged.set(FIELD_TAGS, mergedTags);

        // inizializza component groups
        for (String group : COMPONENT_GROUPS_TO_NAMESPACE) {
            mergedComponents.set(group, objectMapper.createObjectNode());
        }

        Set<String> tagNames = new LinkedHashSet<>();

        for (ServiceSpec s : specs) {
            mergePaths(s, mergedPaths);
            mergeComponents(s, mergedComponents);
            mergeTags(s, mergedTags, tagNames);
        }

        return merged;
    }

    private void mergePaths(ServiceSpec spec, ObjectNode mergedPaths) {
        JsonNode pathsNode = spec.spec.get(FIELD_PATHS);
        if (pathsNode == null || !pathsNode.isObject()) {
            return;
        }
        ObjectNode paths = (ObjectNode) pathsNode;

        paths.fieldNames().forEachRemaining(path -> {
            if (mergedPaths.has(path)) {
                log.warn("OpenAPI path collision (kept first): {} from service={}", path, spec.service);
                return;
            }
            mergedPaths.set(path, paths.get(path));
        });
    }

    private void mergeComponents(ServiceSpec spec, ObjectNode mergedComponents) {
        JsonNode compsNode = spec.spec.get(FIELD_COMPONENTS);
        if (compsNode == null || !compsNode.isObject()) {
            return;
        }
        ObjectNode comps = (ObjectNode) compsNode;

        for (String group : COMPONENT_GROUPS_TO_NAMESPACE) {
            JsonNode groupNode = comps.get(group);
            if (groupNode == null || !groupNode.isObject()) {
                continue;
            }
            ObjectNode groupObj = (ObjectNode) groupNode;
            ObjectNode mergedGroup = (ObjectNode) mergedComponents.get(group);

            groupObj.fieldNames().forEachRemaining(key -> {
                if (mergedGroup.has(key)) {
                    // dopo namespacing non dovrebbe accadere, ma evitiamo override silenziosi
                    log.warn("OpenAPI component collision (ignored): components.{}.{} from service={}", group, key, spec.service);
                    return;
                }
                mergedGroup.set(key, groupObj.get(key));
            });
        }

        // SecuritySchemes: non namespaciamo (sono spesso uguali tra servizi).
        JsonNode secSchemes = comps.get("securitySchemes");
        if (secSchemes != null && secSchemes.isObject()) {
            if (!mergedComponents.has("securitySchemes")) {
                mergedComponents.set("securitySchemes", secSchemes);
            } else {
                log.debug(
                    "OpenAPI securitySchemes ignored from service={} (already defined)",
                    spec.service
                );
            }
        }
    }

    private void mergeTags(ServiceSpec spec, ArrayNode mergedTags, Set<String> tagNames) {
        JsonNode tagsNode = spec.spec.get(FIELD_TAGS);
        if (tagsNode == null || !tagsNode.isArray()) {
            return;
        }
        for (JsonNode tag : tagsNode) {
            String name = tag.path("name").asText(null);
            if (name == null || name.isBlank()) {
                continue;
            }
            if (tagNames.add(name)) {
                mergedTags.add(tag);
            }
        }
    }

    private String toJson(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize merged OpenAPI", e);
        }
    }

    private record ServiceSpec(String service, ObjectNode spec) { }
}
