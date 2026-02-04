package it.sanitech.directory.integrations.keycloak;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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
@Slf4j
public class KeycloakAdminClient {

    private static final String PHONE_ATTR = "phone";
    private static final String PID_ATTR = "pid";
    private static final String DID_ATTR = "did";
    private static final String DEPT_ATTR = "dept";
    private static final String AGGREGATE_TYPE_PATIENT = "PATIENT";
    private static final String AGGREGATE_TYPE_DOCTOR = "DOCTOR";
    private static final String DEFAULT_ROLE_NAME = "default-roles-sanitech";

    private final Keycloak keycloak;
    private final KeycloakAdminProperties properties;

    @Retry(name = "keycloakSync")
    public void syncUser(KeycloakUserSyncRequest request) {
        KeycloakUserRepresentation existing = findUserByEmail(request.email());
        if (existing == null) {
            try {
                String userId = createUser(request);
                removeDefaultRealmRole(userId);
                assignRealmRoleIfRequested(userId, request.roleToAssign());
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
        assignRealmRoleIfRequested(existing.id(), request.roleToAssign());
    }

    /**
     * Disabilita un utente in Keycloak.
     *
     * @param email email dell'utente
     * @param firstName nome (dall'entità locale, per evitare null da Keycloak)
     * @param lastName cognome (dall'entità locale, per evitare null da Keycloak)
     * @param phone telefono (opzionale)
     * @param aggregateType tipo aggregato (DOCTOR/PATIENT)
     * @param aggregateId ID dell'entità locale
     * @param departmentCode codice reparto (solo per DOCTOR)
     */
    @Retry(name = "keycloakSync")
    public void disableUser(String email, String firstName, String lastName, String phone,
                            String aggregateType, Long aggregateId, String departmentCode) {
        KeycloakUserRepresentation existing = findUserByEmail(email);
        if (existing == null) {
            return;
        }
        updateUser(existing.id(), new KeycloakUserSyncRequest(
                email,
                firstName,
                lastName,
                phone,
                false,
                null,
                aggregateType,
                aggregateId,
                departmentCode
        ));
    }

    /**
     * Abilita un utente in Keycloak.
     *
     * @param email email dell'utente
     * @param firstName nome (dall'entità locale, per evitare null da Keycloak)
     * @param lastName cognome (dall'entità locale, per evitare null da Keycloak)
     * @param phone telefono (opzionale)
     * @param aggregateType tipo aggregato (DOCTOR/PATIENT)
     * @param aggregateId ID dell'entità locale
     * @param departmentCode codice reparto (solo per DOCTOR)
     */
    @Retry(name = "keycloakSync")
    public void enableUser(String email, String firstName, String lastName, String phone,
                           String aggregateType, Long aggregateId, String departmentCode) {
        KeycloakUserRepresentation existing = findUserByEmail(email);
        if (existing == null) {
            log.warn("Utente Keycloak con email '{}' non trovato per abilitazione.", email);
            return;
        }
        updateUser(existing.id(), new KeycloakUserSyncRequest(
                email,
                firstName,
                lastName,
                phone,
                true,
                null,
                aggregateType,
                aggregateId,
                departmentCode
        ));
    }

    private String createUser(KeycloakUserSyncRequest request) {
        UserRepresentation payload = toCreateRepresentation(request);
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
            return userId;
        }
    }

    private void updateUser(String userId, KeycloakUserSyncRequest request) {
        UserRepresentation payload = toUpdateRepresentation(request);
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

    /**
     * Crea la rappresentazione per un NUOVO utente Keycloak.
     * Include username perché necessario alla creazione.
     */
    private UserRepresentation toCreateRepresentation(KeycloakUserSyncRequest request) {
        UserRepresentation representation = toUpdateRepresentation(request);
        representation.setUsername(request.email());
        return representation;
    }

    /**
     * Crea la rappresentazione per l'UPDATE di un utente Keycloak esistente.
     * NON include username perché Keycloak non permette di modificarlo.
     */
    private UserRepresentation toUpdateRepresentation(KeycloakUserSyncRequest request) {
        java.util.HashMap<String, List<String>> attributes = new java.util.HashMap<>();
        attributes.put(PHONE_ATTR, request.phone() == null ? List.of() : List.of(request.phone()));

        if (request.aggregateId() != null && request.aggregateType() != null) {
            String idValue = String.valueOf(request.aggregateId());
            if (AGGREGATE_TYPE_PATIENT.equals(request.aggregateType())) {
                attributes.put(PID_ATTR, List.of(idValue));
            } else if (AGGREGATE_TYPE_DOCTOR.equals(request.aggregateType())) {
                attributes.put(DID_ATTR, List.of(idValue));
                if (request.departmentCode() != null && !request.departmentCode().isBlank()) {
                    attributes.put(DEPT_ATTR, List.of(request.departmentCode()));
                }
            }
        }

        UserRepresentation representation = new UserRepresentation();
        representation.setEmail(request.email());
        representation.setFirstName(request.firstName());
        representation.setLastName(request.lastName());
        representation.setEnabled(request.enabled());
        representation.setEmailVerified(true);
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

    private void assignRealmRoleIfRequested(String userId, String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return;
        }
        RoleRepresentation role;
        try {
            role = keycloak.realm(properties.realm()).roles().get(roleName).toRepresentation();
        } catch (WebApplicationException ex) {
            Response response = ex.getResponse();
            if (response != null && response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                log.warn("Ruolo Keycloak '{}' non trovato; utente {} non aggiornato con ruolo.", roleName, userId);
                return;
            }
            throw toSyncException("Errore recupero ruolo Keycloak '" + roleName + "'.", ex);
        } catch (RuntimeException ex) {
            throw toSyncException("Errore recupero ruolo Keycloak '" + roleName + "'.", ex);
        }
        if (role == null) {
            log.warn("Ruolo Keycloak '{}' non trovato; utente {} non aggiornato con ruolo.", roleName, userId);
            return;
        }
        try {
            usersResource().get(userId).roles().realmLevel().add(List.of(role));
        } catch (RuntimeException ex) {
            throw toSyncException("Errore assegnazione ruolo Keycloak '" + roleName + "'.", ex);
        }
    }

    private void removeDefaultRealmRole(String userId) {
        RoleRepresentation defaultRole;
        try {
            defaultRole = keycloak.realm(properties.realm()).roles().get(DEFAULT_ROLE_NAME).toRepresentation();
        } catch (WebApplicationException ex) {
            Response response = ex.getResponse();
            if (response != null && response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                log.debug("Ruolo di default Keycloak '{}' non trovato; nessuna rimozione necessaria.", DEFAULT_ROLE_NAME);
                return;
            }
            throw toSyncException("Errore recupero ruolo di default Keycloak '" + DEFAULT_ROLE_NAME + "'.", ex);
        } catch (RuntimeException ex) {
            throw toSyncException("Errore recupero ruolo di default Keycloak '" + DEFAULT_ROLE_NAME + "'.", ex);
        }
        if (defaultRole == null) {
            log.debug("Ruolo di default Keycloak '{}' non trovato; nessuna rimozione necessaria.", DEFAULT_ROLE_NAME);
            return;
        }
        try {
            usersResource().get(userId).roles().realmLevel().remove(List.of(defaultRole));
            log.debug("Ruolo di default '{}' rimosso dall'utente {}.", DEFAULT_ROLE_NAME, userId);
        } catch (RuntimeException ex) {
            throw toSyncException("Errore rimozione ruolo di default Keycloak '" + DEFAULT_ROLE_NAME + "'.", ex);
        }
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
        credential.setValue(properties.initialPassword());
        credential.setTemporary(false);  // Password permanente, non richiede cambio al primo login
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
