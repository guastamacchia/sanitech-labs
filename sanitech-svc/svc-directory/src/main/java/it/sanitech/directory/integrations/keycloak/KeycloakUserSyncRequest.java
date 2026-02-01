package it.sanitech.directory.integrations.keycloak;

public record KeycloakUserSyncRequest(
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean enabled,
        String roleToAssign,
        String aggregateType,
        Long aggregateId
) {}
