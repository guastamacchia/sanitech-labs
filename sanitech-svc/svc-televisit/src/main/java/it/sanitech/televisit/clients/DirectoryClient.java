package it.sanitech.televisit.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client REST per il servizio directory (svc-directory).
 *
 * <p>
 * Utilizzato per arricchire i payload degli eventi con dati anagrafici
 * (nome, cognome, email) di medici e pazienti, a partire dall'email (subject Keycloak).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DirectoryClient {

    private final RestTemplate restTemplate;

    @Value("${sanitech.directory.url:http://localhost:8082}")
    private String directoryBaseUrl;

    /**
     * Cerca un paziente per email (case-insensitive).
     *
     * @param email email del paziente (subject Keycloak)
     * @return informazioni del paziente o null se non trovato
     */
    public PersonInfo findPatientByEmail(String email) {
        return findPatientByEmail(email, null);
    }

    /**
     * Cerca un paziente per email, propagando il token JWT per autenticazione service-to-service.
     */
    public PersonInfo findPatientByEmail(String email, String bearerToken) {
        if (email == null || email.isBlank()) {
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(directoryBaseUrl)
                    .path("/api/patients/internal/by-email")
                    .queryParam("email", email)
                    .toUriString();

            DirectoryPersonDto response = exchange(url, bearerToken);
            if (response == null) {
                log.debug("Paziente non trovato per email: {}", email);
                return null;
            }
            return new PersonInfo(response.id(), response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per paziente email={}: {}", email, ex.getMessage());
            return null;
        }
    }

    /**
     * Cerca un medico per email (case-insensitive).
     *
     * @param email email del medico (subject Keycloak)
     * @return informazioni del medico o null se non trovato
     */
    public PersonInfo findDoctorByEmail(String email) {
        return findDoctorByEmail(email, null);
    }

    /**
     * Cerca un medico per email, propagando il token JWT per autenticazione service-to-service.
     */
    public PersonInfo findDoctorByEmail(String email, String bearerToken) {
        if (email == null || email.isBlank()) {
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(directoryBaseUrl)
                    .path("/api/doctors/internal/by-email")
                    .queryParam("email", email)
                    .toUriString();

            DirectoryPersonDto response = exchange(url, bearerToken);
            if (response == null) {
                log.debug("Medico non trovato per email: {}", email);
                return null;
            }
            return new PersonInfo(response.id(), response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per medico email={}: {}", email, ex.getMessage());
            return null;
        }
    }

    /**
     * Esegue una GET con o senza Bearer token.
     */
    private DirectoryPersonDto exchange(String url, String bearerToken) {
        if (bearerToken != null && !bearerToken.isBlank()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            ResponseEntity<DirectoryPersonDto> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), DirectoryPersonDto.class);
            return resp.getBody();
        }
        return restTemplate.getForObject(url, DirectoryPersonDto.class);
    }

    /**
     * Record per le informazioni di una persona.
     */
    public record PersonInfo(Long id, String firstName, String lastName, String email) {
        public String fullName() {
            return firstName + " " + lastName;
        }
    }

    /**
     * DTO interno per deserializzare la risposta dal servizio directory.
     */
    private record DirectoryPersonDto(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {}
}
