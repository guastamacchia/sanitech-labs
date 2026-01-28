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
        String authRealm = StringUtils.hasText(properties.authRealm())
                ? properties.authRealm()
                : properties.realm();
        String grantType = StringUtils.hasText(properties.grantType())
                ? properties.grantType()
                : OAuth2Constants.PASSWORD;

        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(properties.serverUrl())
                .realm(authRealm)
                .clientId(properties.clientId())
                .grantType(grantType);

        if (OAuth2Constants.CLIENT_CREDENTIALS.equals(grantType)) {
            if (!StringUtils.hasText(properties.clientSecret())) {
                throw new IllegalStateException("clientSecret required with grant_type=client_credentials");
            }
            builder.clientSecret(properties.clientSecret());
        } else {
            if (!StringUtils.hasText(properties.username()) || !StringUtils.hasText(properties.password())) {
                throw new IllegalStateException("username/password required with grant_type=password");
            }
            builder.username(properties.username());
            builder.password(properties.password());
            if (StringUtils.hasText(properties.clientSecret())) {
                builder.clientSecret(properties.clientSecret());
            }
        }

        return builder.build();
    }
}
