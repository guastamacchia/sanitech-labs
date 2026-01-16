package it.sanitech.gateway.config;

import it.sanitech.gateway.openapi.OpenApiHttpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configurazione del {@link WebClient} usato dal gateway per scaricare le specifiche OpenAPI
 * dai microservizi downstream.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiWebClientConfig {

    private final OpenApiHttpProperties httpProps;

    @Bean
    public WebClient openApiWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(httpProps.getResponseTimeoutMs()));

        // connect timeout: configurazione tramite option su channel
        httpClient = httpClient.option(
                io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                httpProps.getConnectTimeoutMs()
        );

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
