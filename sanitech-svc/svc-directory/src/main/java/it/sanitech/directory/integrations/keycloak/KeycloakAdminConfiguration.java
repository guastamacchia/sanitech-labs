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
        boolean useClientCredentials = OAuth2Constants.CLIENT_CREDENTIALS.equals(properties.grantType())
                || !StringUtils.hasText(properties.username())
                || !StringUtils.hasText(properties.password());
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(properties.serverUrl())
                .realm(authRealm)
                .clientId(properties.clientId())
                .grantType(useClientCredentials ? OAuth2Constants.CLIENT_CREDENTIALS : OAuth2Constants.PASSWORD);

        if (!useClientCredentials) {
            builder.username(properties.username())
                    .password(properties.password());
        }

        if (StringUtils.hasText(properties.clientSecret())) {
            builder.clientSecret(properties.clientSecret());
        }

        return builder.build();
    }
}
