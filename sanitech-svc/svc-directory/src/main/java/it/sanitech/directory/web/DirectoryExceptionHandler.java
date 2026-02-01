package it.sanitech.directory.web;

import it.sanitech.commons.exception.ProblemDetails;
import it.sanitech.directory.integrations.captcha.CaptchaVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * Gestore eccezioni specifiche per svc-directory.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DirectoryExceptionHandler {

    private static final String TYPE_CAPTCHA_FAILED = "captcha-failed";
    private static final String TITLE_CAPTCHA_FAILED = "Verifica CAPTCHA fallita";

    /**
     * Gestisce errori di verifica CAPTCHA (400).
     */
    @ExceptionHandler(CaptchaVerificationException.class)
    public ResponseEntity<ProblemDetails> captchaFailed(CaptchaVerificationException ex, HttpServletRequest request) {
        log.warn("CAPTCHA verification failed on {}: {}", safeUri(request), ex.getMessage());

        ProblemDetails problem = ProblemDetails.builder()
                .type(TYPE_CAPTCHA_FAILED)
                .title(TITLE_CAPTCHA_FAILED)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(safeUri(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    private static String safeUri(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getRequestURI)
                .orElse("");
    }
}
