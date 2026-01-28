package it.sanitech.directory.integrations.keycloak;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfiguration {

    @Bean(destroyMethod = "close")
    public Keycloak keycloak(KeycloakAdminProperties properties) {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(properties.serverUrl())
                .realm(properties.realm())
                .clientId(properties.clientId())
                .username(properties.username())
                .password(properties.password())
                .grantType(OAuth2Constants.PASSWORD);

        if (StringUtils.hasText(properties.clientSecret())) {
            builder.clientSecret(properties.clientSecret());
        }

        return builder.build();
    }
}
