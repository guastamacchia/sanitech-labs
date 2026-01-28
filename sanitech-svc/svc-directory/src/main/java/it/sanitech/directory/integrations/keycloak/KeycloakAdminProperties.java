package it.sanitech.directory.integrations.keycloak;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sanitech.keycloak.admin")
public record KeycloakAdminProperties(
        @NotBlank String serverUrl,
        @NotBlank String realm,
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String tokenPath
) {}
