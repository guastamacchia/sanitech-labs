package it.sanitech.notifications.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client REST per il servizio directory (svc-directory).
 *
 * <p>
 * Utilizzato per recuperare informazioni di medici e pazienti
 * a partire dal nome completo, necessario per l'invio delle email televisita.
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
     * Cerca un medico per nome e cognome.
     *
     * @param firstName nome del medico
     * @param lastName cognome del medico
     * @return informazioni del medico o null se non trovato
     */
    public PersonInfo findDoctorByName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(directoryBaseUrl)
                    .path("/api/doctors/internal/by-name")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .toUriString();

            DirectoryPersonDto response = restTemplate.getForObject(url, DirectoryPersonDto.class);
            if (response == null) {
                log.debug("Medico non trovato: {} {}", firstName, lastName);
                return null;
            }
            return new PersonInfo(response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per medico {} {}: {}", firstName, lastName, ex.getMessage());
            return null;
        }
    }

    /**
     * Cerca un paziente per nome e cognome.
     *
     * @param firstName nome del paziente
     * @param lastName cognome del paziente
     * @return informazioni del paziente o null se non trovato
     */
    public PersonInfo findPatientByName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(directoryBaseUrl)
                    .path("/api/patients/internal/by-name")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .toUriString();

            DirectoryPersonDto response = restTemplate.getForObject(url, DirectoryPersonDto.class);
            if (response == null) {
                log.debug("Paziente non trovato: {} {}", firstName, lastName);
                return null;
            }
            return new PersonInfo(response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per paziente {} {}: {}", firstName, lastName, ex.getMessage());
            return null;
        }
    }

    /**
     * Cerca una persona (medico o paziente) dato il nome completo.
     * Estrae nome e cognome dalla stringa "Nome Cognome".
     *
     * @param fullName nome completo
     * @param isDoctor true per medico, false per paziente
     * @return informazioni della persona o null se non trovato
     */
    public PersonInfo findByFullName(String fullName, boolean isDoctor) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length < 2) {
            log.warn("Nome completo non valido (manca cognome): {}", fullName);
            return null;
        }

        String firstName = parts[0];
        String lastName = parts[1];

        return isDoctor ? findDoctorByName(firstName, lastName) : findPatientByName(firstName, lastName);
    }

    /**
     * Record per le informazioni di una persona.
     */
    public record PersonInfo(String firstName, String lastName, String email) {}

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
