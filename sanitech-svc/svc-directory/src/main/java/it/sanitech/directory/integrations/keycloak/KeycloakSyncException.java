package it.sanitech.directory.integrations.keycloak;

public class KeycloakSyncException extends RuntimeException {

    public KeycloakSyncException(String message) {
        super(message);
    }

    public KeycloakSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
