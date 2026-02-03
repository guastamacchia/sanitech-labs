package it.sanitech.docs.exception;

import it.sanitech.commons.exception.ProblemDetails;
import it.sanitech.commons.utilities.AppConstants;
import it.sanitech.outbox.core.DomainEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;

/**
 * Gestore eccezioni specifiche per il dominio docs.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DocsExceptionHandler {

    private static final String AGG_DOCUMENT = "DOCUMENT";
    private static final String EVT_ACCESS_DENIED = "DOCUMENT_ACCESS_DENIED";

    private final DomainEventPublisher eventPublisher;

    @ExceptionHandler(ConsentDeniedException.class)
    public ResponseEntity<ProblemDetails> consentDenied(ConsentDeniedException ex, HttpServletRequest request) {

        // Registra evento di accesso negato per audit
        publishAccessDeniedEvent(request, ex.getMessage());

        return build(
                HttpStatus.FORBIDDEN,
                it.sanitech.docs.utilities.AppConstants.Problem.TYPE_CONSENT_REQUIRED,
                it.sanitech.docs.utilities.AppConstants.ErrorMessage.ERR_CONSENT_REQUIRED,
                ex.getMessage(),
                request
        );
    }

    private void publishAccessDeniedEvent(HttpServletRequest request, String reason) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String actorId = Optional.ofNullable(auth)
                    .map(Authentication::getName)
                    .orElse("anonymous");

            String patientId = Optional.ofNullable(request.getParameter("patientId")).orElse("unknown");
            String requestUri = Optional.ofNullable(request.getRequestURI()).orElse("");
            String clientIp = Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");

            eventPublisher.publish(AGG_DOCUMENT, "access-denied-" + System.currentTimeMillis(), EVT_ACCESS_DENIED, Map.of(
                    "actorId", actorId,
                    "patientId", patientId,
                    "requestUri", requestUri,
                    "clientIp", clientIp,
                    "reason", reason,
                    "outcome", "DENIED"
            ), it.sanitech.docs.utilities.AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

            log.info("Audit: accesso documenti negato. actor={}, patientId={}, uri={}", actorId, patientId, requestUri);
        } catch (Exception e) {
            // Non blocchiamo la risposta in caso di errore nell'audit
            log.warn("Impossibile registrare evento di accesso negato: {}", e.getMessage());
        }
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetails> externalService(ExternalServiceException ex, HttpServletRequest request) {
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE,
                AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE,
                ex.getMessage(),
                request
        );
    }

    private static ResponseEntity<ProblemDetails> build(HttpStatus status,
                                                        String type,
                                                        String title,
                                                        String detail,
                                                        HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ProblemDetails.builder()
                        .type(type)
                        .title(title)
                        .status(status.value())
                        .detail(detail)
                        .instance(Optional.ofNullable(request)
                                .map(HttpServletRequest::getRequestURI)
                                .orElse(""))
                        .build());
    }
}
