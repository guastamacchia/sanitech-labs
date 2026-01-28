package it.sanitech.directory.integrations.keycloak;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private static final String PHONE_ATTR = "phone";

    private final Keycloak keycloak;
    private final KeycloakAdminProperties properties;

    @Retry(name = "keycloakSync")
    public void syncUser(KeycloakUserSyncRequest request) {
        KeycloakUserRepresentation existing = findUserByEmail(request.email());
        if (existing == null) {
            try {
                createUser(request);
                return;
            } catch (RuntimeException ex) {
                if (!(ex instanceof WebApplicationException webException)
                        || webException.getResponse() == null
                        || webException.getResponse().getStatus() != Response.Status.CONFLICT.getStatusCode()) {
                    throw toSyncException("Errore creazione utente Keycloak.", ex);
                }
                existing = findUserByEmail(request.email());
                if (existing == null) {
                    throw new KeycloakSyncException("Conflitto durante la creazione utente Keycloak.", ex);
                }
            }
        }
        updateUser(existing.id(), request);
    }

    @Retry(name = "keycloakSync")
    public void disableUser(String email) {
        KeycloakUserRepresentation existing = findUserByEmail(email);
        if (existing == null) {
            return;
        }
        updateUser(existing.id(), new KeycloakUserSyncRequest(
                email,
                existing.firstName(),
                existing.lastName(),
                existing.attributes() != null && existing.attributes().containsKey(PHONE_ATTR)
                        ? existing.attributes().get(PHONE_ATTR).stream().findFirst().orElse(null)
                        : null,
                false
        ));
    }

    private void createUser(KeycloakUserSyncRequest request) {
        UserRepresentation payload = toRepresentation(request);
        try (Response response = usersResource().create(payload)) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new WebApplicationException(response);
            }
            String userId = extractUserId(response.getLocation());
            if (userId == null) {
                throw new KeycloakSyncException("Impossibile recuperare l'ID utente Keycloak dalla location.");
            }
            try {
                usersResource().get(userId).resetPassword(buildInitialPassword());
            } catch (RuntimeException ex) {
                throw toSyncException("Errore impostazione password utente Keycloak.", ex);
            }
        }
    }

    private void updateUser(String userId, KeycloakUserSyncRequest request) {
        UserRepresentation payload = toRepresentation(request);
        try {
            usersResource().get(userId).update(payload);
        } catch (RuntimeException ex) {
            throw toSyncException("Errore aggiornamento utente Keycloak.", ex);
        }
    }

    private KeycloakUserRepresentation findUserByEmail(String email) {
        try {
            List<UserRepresentation> users = usersResource().searchByEmail(email, true);
            if (users == null || users.isEmpty()) {
                return null;
            }
            for (UserRepresentation rep : users) {
                if (rep != null && Objects.equals(email, rep.getEmail())) {
                    return fromRepresentation(rep);
                }
            }
            return users.get(0) != null ? fromRepresentation(users.get(0)) : null;
        } catch (RuntimeException ex) {
            throw toSyncException("Errore ricerca utente Keycloak.", ex);
        }
    }

    private UserRepresentation toRepresentation(KeycloakUserSyncRequest request) {
        Map<String, List<String>> attributes = Map.of(
                PHONE_ATTR,
                request.phone() == null ? List.of() : List.of(request.phone())
        );
        UserRepresentation representation = new UserRepresentation();
        representation.setEmail(request.email());
        representation.setUsername(request.email());
        representation.setFirstName(request.firstName());
        representation.setLastName(request.lastName());
        representation.setEnabled(request.enabled());
        representation.setAttributes(attributes);
        return representation;
    }

    private KeycloakUserRepresentation fromRepresentation(UserRepresentation rep) {
        return new KeycloakUserRepresentation(
                rep.getId(),
                rep.getUsername(),
                rep.getEmail(),
                rep.getFirstName(),
                rep.getLastName(),
                rep.isEnabled(),
                rep.getAttributes()
        );
    }

    private UsersResource usersResource() {
        return keycloak.realm(properties.realm()).users();
    }

    private String extractUserId(URI location) {
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        if (path == null) {
            return null;
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash + 1 >= path.length()) {
            return null;
        }
        return path.substring(lastSlash + 1);
    }

    private CredentialRepresentation buildInitialPassword() {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("qwerty");
        credential.setTemporary(false);
        return credential;
    }

    private KeycloakSyncException toSyncException(String message, RuntimeException ex) {
        if (ex instanceof WebApplicationException webException) {
            Response response = webException.getResponse();
            if (response != null) {
                return new KeycloakSyncException(message + " (status=" + response.getStatus() + ")", ex);
            }
        }
        return new KeycloakSyncException(message, ex);
    }
}
