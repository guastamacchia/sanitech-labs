package it.sanitech.docs.integrations.consents;

import it.sanitech.docs.exception.ConsentDeniedException;
import it.sanitech.docs.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client sincrono verso {@code svc-consents} per verificare se un medico può accedere ai dati di un paziente.
 *
 * <p>
 * Importante: la policy del consenso è applicata nei servizi "clinici" (come questo),
 * non in {@code svc-directory}.
 * </p>
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ConsentsProperties.class)
public class ConsentClient {

    private static final String SCOPE_DOCS = "DOCS";

    private final ConsentsProperties props;
    private final RestClient.Builder restClientBuilder;

    /**
     * Verifica il consenso per un paziente (use-case: medico che consulta documenti).
     *
     * <p>
     * Resilienza:
     * <ul>
     *   <li>{@link CircuitBreaker} per evitare di sovraccaricare {@code svc-consents} in caso di malfunzionamento;</li>
     *   <li>{@link Retry} per errori transitori (timeout/rete).</li>
     * </ul>
     * </p>
     *
     * @param patientId id paziente
     * @param auth token utente (propagato verso svc-consents)
     * @throws ConsentDeniedException se il consenso non è valido
     * @throws ExternalServiceException se non è possibile verificare il consenso
     */
    @CircuitBreaker(name = "consents")
    @Retry(name = "consents")
    public void assertConsentForDocs(Long patientId, JwtAuthenticationToken auth) {

        String tokenValue = auth.getToken().getTokenValue();

        try {
            RestClient rc = restClientBuilder.baseUrl(props.getBaseUrl()).build();

            ConsentCheckResponse resp = rc.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/consents/check")
                            .queryParam("patientId", patientId)
                            .queryParam("scope", SCOPE_DOCS)
                            .build())
                    .headers(h -> h.setBearerAuth(tokenValue))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ExternalServiceException("Errore da svc-consents: HTTP " + res.getStatusCode(), null);
                    })
                    .body(ConsentCheckResponse.class);

            if (resp == null || !resp.allowed()) {
                throw ConsentDeniedException.forPatient(patientId);
            }
        } catch (ConsentDeniedException e) {
            throw e;
        } catch (RestClientException e) {
            throw new ExternalServiceException("Impossibile verificare il consenso tramite svc-consents.", e);
        }
    }
}
