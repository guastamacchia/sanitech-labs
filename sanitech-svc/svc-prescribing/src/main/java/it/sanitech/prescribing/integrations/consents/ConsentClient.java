package it.sanitech.prescribing.integrations.consents;

import it.sanitech.prescribing.exception.ConsentDeniedException;
import it.sanitech.prescribing.exception.ExternalServiceException;
import it.sanitech.prescribing.security.JwtClaimUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client sincrono verso {@code svc-consents}.
 *
 * <p>
 * In questo microservizio il consenso è verificato quando un medico accede/crea/modifica
 * prescrizioni di un paziente.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ConsentClient {

    private static final String CONSENTS_CB = "consents";
    private static final String CONSENTS_RETRY = "consents";

    private final RestClient consentsRestClient;

    /**
     * Verifica e <b>impone</b> la presenza di consenso.
     *
     * <p>
     * Il metodo è annotato con Resilience4j per evitare self-invocation:
     * viene invocato dal service via proxy Spring AOP.
     * </p>
     *
     * @throws ConsentDeniedException se il consenso non è presente
     * @throws ExternalServiceException se {@code svc-consents} non è raggiungibile / circuit open
     */
    @CircuitBreaker(name = CONSENTS_CB, fallbackMethod = "fallbackAssert")
    @Retry(name = CONSENTS_RETRY)
    public void assertPrescriptionConsent(Long patientId, Long doctorId, Authentication auth) {
        String token = JwtClaimUtils.bearerToken(auth)
                .orElseThrow(() -> ExternalServiceException.missingBearerToken("Token JWT mancante: impossibile interrogare svc-consents."));

        boolean allowed;
        try {
            ConsentCheckResponse resp = consentsRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/consents/check")
                            .queryParam("patientId", patientId)
                            .queryParam("doctorId", doctorId)
                            .queryParam("scope", "PRESCRIPTIONS")
                            .build()
                    )
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(ConsentCheckResponse.class);

            allowed = (resp != null && resp.allowed());

        } catch (RestClientException ex) {
            throw ExternalServiceException.downstream("svc-consents", ex);
        }

        if (!allowed) {
            throw ConsentDeniedException.forPatient(patientId);
        }
    }

    @SuppressWarnings("unused")
    private void fallbackAssert(Long patientId, Long doctorId, Authentication auth, Throwable cause) {
        // In caso di circuito aperto o errore ripetuto, preferiamo non consentire l'operazione.
        throw ExternalServiceException.unavailable("svc-consents", cause);
    }

    /**
     * DTO di risposta minimale dell'endpoint /api/consents/check.
     */
    public record ConsentCheckResponse(boolean allowed) {
    }
}
