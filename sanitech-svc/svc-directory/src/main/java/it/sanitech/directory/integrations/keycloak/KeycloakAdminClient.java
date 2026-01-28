package it.sanitech.directory.integrations.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private static final String PHONE_ATTR = "phone";

    private final RestTemplate keycloakRestTemplate;
    private final KeycloakAdminProperties properties;

    @Retry(name = "keycloakSync")
    public void syncUser(KeycloakUserSyncRequest request) {
        KeycloakUserRepresentation existing = findUserByEmail(request.email());
        if (existing == null) {
            try {
                createUser(request);
                return;
            } catch (HttpClientErrorException.Conflict ex) {
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
        KeycloakUserRepresentation payload = toRepresentation(request);
        HttpHeaders headers = bearerHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KeycloakUserRepresentation> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = keycloakRestTemplate.exchange(
                adminUsersUrl(),
                HttpMethod.POST,
                entity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new KeycloakSyncException("Errore creazione utente Keycloak: " + response.getStatusCode());
        }
    }

    private void updateUser(String userId, KeycloakUserSyncRequest request) {
        KeycloakUserRepresentation payload = toRepresentation(request);
        HttpHeaders headers = bearerHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<KeycloakUserRepresentation> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> response = keycloakRestTemplate.exchange(
                adminUsersUrl() + "/" + userId,
                HttpMethod.PUT,
                entity,
                Void.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new KeycloakSyncException("Errore aggiornamento utente Keycloak: " + response.getStatusCode());
        }
    }

    private KeycloakUserRepresentation findUserByEmail(String email) {
        HttpHeaders headers = bearerHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(adminUsersUrl())
                .queryParam("email", email)
                .queryParam("exact", true)
                .toUriString();
        ResponseEntity<KeycloakUserRepresentation[]> response = keycloakRestTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KeycloakUserRepresentation[].class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return null;
        }
        for (KeycloakUserRepresentation rep : response.getBody()) {
            if (rep != null && Objects.equals(email, rep.email())) {
                return rep;
            }
        }
        return response.getBody().length > 0 ? response.getBody()[0] : null;
    }

    private KeycloakUserRepresentation toRepresentation(KeycloakUserSyncRequest request) {
        Map<String, List<String>> attributes = Map.of(
                PHONE_ATTR,
                request.phone() == null ? List.of() : List.of(request.phone())
        );
        return new KeycloakUserRepresentation(
                null,
                request.email(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.enabled(),
                attributes
        );
    }

    private String adminUsersUrl() {
        return properties.serverUrl() + "/admin/realms/" + properties.realm() + "/users";
    }

    private HttpHeaders bearerHeaders() {
        String token = obtainToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String obtainToken() {
        String tokenUrl = properties.serverUrl() + properties.tokenPath().replace("{realm}", properties.realm());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        try {
            ResponseEntity<KeycloakTokenResponse> response = keycloakRestTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    KeycloakTokenResponse.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new KeycloakSyncException("Errore ottenimento token Keycloak: " + response.getStatusCode());
            }
            return response.getBody().accessToken();
        } catch (HttpClientErrorException ex) {
            throw new KeycloakSyncException(
                    "Errore ottenimento token Keycloak: " + ex.getStatusCode()
                            + " (clientId=" + properties.clientId()
                            + ", realm=" + properties.realm()
                            + ", url=" + tokenUrl + ")",
                    ex
            );
        }
    }

    private record KeycloakTokenResponse(@JsonProperty("access_token") String accessToken) {}
}
