package it.sanitech.docs.exception;

import it.sanitech.commons.exception.ProblemDetails;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * Gestore eccezioni specifiche per il dominio docs.
 */
@RestControllerAdvice
public class DocsExceptionHandler {

    @ExceptionHandler(ConsentDeniedException.class)
    public ResponseEntity<ProblemDetails> consentDenied(ConsentDeniedException ex, HttpServletRequest request) {
        return build(
                HttpStatus.FORBIDDEN,
                it.sanitech.docs.utilities.AppConstants.Problem.TYPE_CONSENT_REQUIRED,
                it.sanitech.docs.utilities.AppConstants.ErrorMessage.ERR_CONSENT_REQUIRED,
                ex.getMessage(),
                request
        );
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
