package it.sanitech.gateway.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OpenApiMergeServiceTest {

    @Test
    void serviceJsonReturnsPayloadFromConfiguredTarget() {
        OpenApiTargetsProperties props = new OpenApiTargetsProperties();
        props.getTargets().put("docs", "http://svc-docs/openapi");

        ExchangeFunction exchange = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"openapi\":\"3.0.1\"}")
                        .build()
        );
        WebClient webClient = WebClient.builder().exchangeFunction(exchange).build();

        OpenApiMergeService service = new OpenApiMergeService(webClient, new ObjectMapper(), props);

        StepVerifier.create(service.serviceJson("docs"))
                .expectNext("{\"openapi\":\"3.0.1\"}")
                .verifyComplete();
    }

    @Test
    void mergedJsonNamespacesComponentsAndCaches() throws Exception {
        OpenApiTargetsProperties props = new OpenApiTargetsProperties();
        props.getTargets().put("alpha", "http://svc-alpha/openapi");
        props.getTargets().put("beta", "http://svc-beta/openapi");
        props.setMergedCacheTtlSeconds(60);

        ExchangeFunction exchange = request -> {
            String body;
            if (request.url().toString().contains("alpha")) {
                body = "{" +
                        "\"openapi\":\"3.0.1\"," +
                        "\"paths\":{\"/alpha\":{}}," +
                        "\"components\":{\"schemas\":{\"Ping\":{\"type\":\"object\"}}}" +
                        "}";
            } else {
                body = "{" +
                        "\"openapi\":\"3.0.1\"," +
                        "\"paths\":{\"/beta\":{}}," +
                        "\"components\":{\"schemas\":{\"Pong\":{\"type\":\"object\"}}}" +
                        "}";
            }
            return Mono.just(
                    ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(body)
                            .build()
            );
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchange).build();
        ObjectMapper mapper = new ObjectMapper();

        OpenApiMergeService service = new OpenApiMergeService(webClient, mapper, props);

        String merged = service.mergedJson().block();
        JsonNode node = mapper.readTree(merged);

        assertThat(node.path("paths").has("/alpha")).isTrue();
        assertThat(node.path("paths").has("/beta")).isTrue();
        assertThat(node.path("components").path("schemas").has("alpha_Ping")).isTrue();
        assertThat(node.path("components").path("schemas").has("beta_Pong")).isTrue();

        StepVerifier.create(service.mergedJson())
                .expectNext(merged)
                .verifyComplete();
    }
}
