package it.sanitech.prescribing.exception;

import it.sanitech.commons.exception.ProblemDetails;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * Gestione delle eccezioni specifiche del dominio Prescribing.
 */
@Slf4j
@RestControllerAdvice
public class PrescribingExceptionHandler {

    @ExceptionHandler(ConsentDeniedException.class)
    public ResponseEntity<ProblemDetails> handleConsentDenied(ConsentDeniedException ex, HttpServletRequest req) {
        return build(
                HttpStatus.FORBIDDEN,
                AppConstants.Problem.TYPE_FORBIDDEN,
                AppConstants.ErrorMessage.ERR_FORBIDDEN,
                ex.getMessage(),
                req
        );
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetails> handleExternalService(ExternalServiceException ex, HttpServletRequest req) {
        log.warn("Errore servizio downstream su {}: {}", safeUri(req), ex.getMessage(), ex);
        return build(
                HttpStatus.SERVICE_UNAVAILABLE,
                AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE,
                AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE,
                ex.getMessage(),
                req
        );
    }

    private static ResponseEntity<ProblemDetails> build(HttpStatus status,
                                                        String type,
                                                        String title,
                                                        String detail,
                                                        HttpServletRequest request) {
        ProblemDetails body = ProblemDetails.builder()
                .type(type)
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(safeUri(request))
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private static String safeUri(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getRequestURI)
                .orElse("");
    }
}
