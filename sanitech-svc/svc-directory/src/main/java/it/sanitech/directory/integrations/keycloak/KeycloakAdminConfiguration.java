package it.sanitech.directory.integrations.keycloak;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfiguration {

    @Bean
    public RestTemplate keycloakRestTemplate() {
        return new RestTemplate();
    }
}
