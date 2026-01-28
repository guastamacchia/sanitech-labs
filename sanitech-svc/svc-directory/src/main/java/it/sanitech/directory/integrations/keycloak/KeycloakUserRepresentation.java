package it.sanitech.directory.integrations.keycloak;

import java.util.List;
import java.util.Map;

public record KeycloakUserRepresentation(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Map<String, List<String>> attributes
) {}
