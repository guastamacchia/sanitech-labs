package it.sanitech.gateway.openapi;

import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = OpenApiProxyController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration.class
        }
)
class OpenApiProxyControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OpenApiMergeService mergeService;

    @Test
    void byServiceReturnsJsonWhenWhitelisted() {
        when(mergeService.serviceJson("docs")).thenReturn(Mono.just("{\"openapi\":\"3.0.1\"}"));

        webTestClient.get()
                .uri("/openapi/docs")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.openapi").isEqualTo("3.0.1");
    }

    @Test
    void byServiceReturnsNotFoundWhenMissing() {
        webTestClient.get()
                .uri("/openapi/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void mergedReturnsMergedSpec() {
        when(mergeService.mergedJson()).thenReturn(Mono.just("{\"openapi\":\"3.0.1\"}"));

        webTestClient.get()
                .uri("/openapi/merged")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.openapi").isEqualTo("3.0.1");
    }

    @TestConfiguration
    static class PropsConfig {
        @Bean
        OpenApiTargetsProperties openApiTargetsProperties() {
            OpenApiTargetsProperties props = new OpenApiTargetsProperties();
            props.setTargets(Map.of("docs", "http://svc-docs/openapi"));
            return props;
        }
    }
}
