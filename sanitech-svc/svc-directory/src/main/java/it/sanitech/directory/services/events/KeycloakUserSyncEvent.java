package it.sanitech.directory.services.events;

public record KeycloakUserSyncEvent(
        String aggregateType,
        Long aggregateId,
        String email,
        String firstName,
        String lastName,
        String phone,
        boolean enabled,
        String roleToAssign,
        String previousEmail,
        String departmentCode
) {
}
