package it.sanitech.prescribing.integrations.consents;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Bean factory per i client HTTP verso servizi downstream.
 */
@Configuration
@EnableConfigurationProperties(ConsentsProperties.class)
public class ConsentsClientConfig {

    @Bean
    public RestClient consentsRestClient(RestClient.Builder builder, ConsentsProperties props) {
        return builder
                .baseUrl(props.getBaseUrl())
                .build();
    }
}
