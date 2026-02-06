package it.sanitech.admissions.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client REST per il servizio directory (svc-directory).
 *
 * <p>
 * Utilizzato per arricchire i payload degli eventi con dati anagrafici
 * (nome, cognome, email) di medici e pazienti, a partire dall'ID numerico.
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
     * Cerca un paziente per ID numerico.
     *
     * @param patientId ID del paziente
     * @return informazioni del paziente o null se non trovato
     */
    public PersonInfo findPatientById(Long patientId) {
        if (patientId == null || patientId <= 0) {
            return null;
        }

        try {
            String url = directoryBaseUrl + "/api/patients/" + patientId;
            DirectoryPersonDto response = restTemplate.getForObject(url, DirectoryPersonDto.class);
            if (response == null) {
                log.debug("Paziente non trovato per id: {}", patientId);
                return null;
            }
            return new PersonInfo(response.id(), response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per paziente id={}: {}", patientId, ex.getMessage());
            return null;
        }
    }

    /**
     * Cerca un medico per ID numerico.
     *
     * @param doctorId ID del medico
     * @return informazioni del medico o null se non trovato
     */
    public PersonInfo findDoctorById(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            return null;
        }

        try {
            String url = directoryBaseUrl + "/api/doctors/" + doctorId;
            DirectoryPersonDto response = restTemplate.getForObject(url, DirectoryPersonDto.class);
            if (response == null) {
                log.debug("Medico non trovato per id: {}", doctorId);
                return null;
            }
            return new PersonInfo(response.id(), response.firstName(), response.lastName(), response.email());

        } catch (RestClientException ex) {
            log.warn("Errore chiamata directory per medico id={}: {}", doctorId, ex.getMessage());
            return null;
        }
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
