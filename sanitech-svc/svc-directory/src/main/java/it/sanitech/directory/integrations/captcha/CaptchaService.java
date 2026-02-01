package it.sanitech.directory.integrations.captcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Servizio per la verifica di reCAPTCHA v3.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaService {

    private final CaptchaProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verifica il token CAPTCHA.
     *
     * @param token il token CAPTCHA dal frontend
     * @throws CaptchaVerificationException se la verifica fallisce
     */
    public void verify(String token) {
        if (!properties.enabled()) {
            log.debug("CAPTCHA verification disabled, skipping");
            return;
        }

        if (!StringUtils.hasText(token)) {
            throw new CaptchaVerificationException("Token CAPTCHA mancante");
        }

        if (!StringUtils.hasText(properties.secretKey())) {
            log.warn("CAPTCHA secret key not configured, skipping verification");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", properties.secretKey());
            params.add("response", token);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<CaptchaResponse> response = restTemplate.postForEntity(
                    properties.verifyUrl(),
                    request,
                    CaptchaResponse.class
            );

            CaptchaResponse body = response.getBody();
            if (body == null) {
                throw new CaptchaVerificationException("Risposta CAPTCHA vuota");
            }

            if (!body.success()) {
                log.warn("CAPTCHA verification failed: {}", body.errorCodes());
                throw new CaptchaVerificationException("Verifica CAPTCHA fallita");
            }

            if (body.score() != null && body.score() < properties.scoreThreshold()) {
                log.warn("CAPTCHA score {} below threshold {}", body.score(), properties.scoreThreshold());
                throw new CaptchaVerificationException("Punteggio CAPTCHA insufficiente");
            }

            log.debug("CAPTCHA verified successfully with score {}", body.score());

        } catch (RestClientException e) {
            log.error("Error calling CAPTCHA verification service", e);
            throw new CaptchaVerificationException("Errore durante la verifica CAPTCHA", e);
        }
    }

    /**
     * Risposta dell'API reCAPTCHA.
     */
    private record CaptchaResponse(
            boolean success,
            Double score,
            String action,
            @JsonProperty("challenge_ts") String challengeTs,
            String hostname,
            @JsonProperty("error-codes") List<String> errorCodes
    ) {}
}
