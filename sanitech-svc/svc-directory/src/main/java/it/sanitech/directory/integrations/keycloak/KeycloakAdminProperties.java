package it.sanitech.directory.integrations.keycloak;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sanitech.keycloak.admin")
public record KeycloakAdminProperties(
        @NotBlank String serverUrl,
        @Nullable String authRealm,
        @NotBlank String realm,
        @NotBlank String clientId,
        @Nullable String clientSecret,
        @Nullable String username,
        @Nullable String password,
        @Nullable String grantType,
        @NotBlank String initialPassword
) {}
