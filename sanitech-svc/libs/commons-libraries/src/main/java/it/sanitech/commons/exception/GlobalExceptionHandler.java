package it.sanitech.commons.exception;

import it.sanitech.commons.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;

/**
 * Gestore globale delle eccezioni del microservizio.
 *
 * <p>
 * Produce risposte in formato RFC 7807 (Problem Details), garantendo un formato uniforme
 * per errori applicativi, validazione e resilienza (rate limit, circuit breaker).
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Risorsa non trovata (404).
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetails> notFound(NotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND,
                AppConstants.Problem.TYPE_NOT_FOUND,
                AppConstants.ErrorMessage.ERR_NOT_FOUND,
                ex.getMessage(),
                request,
                null);
    }

    /**
     * Accesso negato per policy di reparto (403).
     */
    @ExceptionHandler(DepartmentAccessDeniedException.class)
    public ResponseEntity<ProblemDetails> deptDenied(DepartmentAccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN,
                AppConstants.Problem.TYPE_FORBIDDEN,
                AppConstants.ErrorMessage.ERR_FORBIDDEN,
                ex.getMessage(),
                request,
                null);
    }

    /**
     * Accesso negato (403).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetails> accessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN,
                AppConstants.Problem.TYPE_FORBIDDEN,
                AppConstants.ErrorMessage.ERR_FORBIDDEN,
                ex.getMessage(),
                request,
                null);
    }

    /**
     * Errori di validazione (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                AppConstants.Problem.TYPE_VALIDATION_ERROR,
                AppConstants.ErrorMessage.ERR_VALIDATION,
                AppConstants.ErrorMessage.MSG_VALIDATION_FAILED,
                request,
                ex.getBindingResult().getFieldErrors().stream()
                    .map(GlobalExceptionHandler::toFieldErrorExtra)
                    .toList()
        );
    }

    /**
     * Input non valido (400).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                AppConstants.Problem.TYPE_BAD_REQUEST,
                AppConstants.ErrorMessage.ERR_BAD_REQUEST,
                ex.getMessage(),
                request,
                null);
    }

    /**
     * Conflitto di dominio (409).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetails> conflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
                AppConstants.Problem.TYPE_CONFLICT,
                AppConstants.ErrorMessage.ERR_CONFLICT,
                ex.getMessage(),
                request,
                null);
    }

    /**
     * Violazioni vincoli database (es. unique email) → 409 Conflict.
     *
     * <p>
     * Nota: la detail è volutamente generica per non esporre dettagli interni del DB.
     * </p>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Conflitto dati su {}: {}", safeUri(request), ex.getMostSpecificCause().getMessage());

        return build(
                HttpStatus.CONFLICT,
                AppConstants.Problem.TYPE_CONFLICT,
                AppConstants.ErrorMessage.ERR_CONFLICT,
                AppConstants.ErrorMessage.MSG_CONFLICT,
                request,
                null
        );
    }

    /**
     * Rate limiter → troppe richieste (429).
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetails> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS,
                AppConstants.Problem.TYPE_TOO_MANY_REQUESTS,
                AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS,
                AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS,
                request,
                null);
    }

    /**
     * Circuit breaker aperto (503).
     *
     * <p>
     * Manteniamo l'handler per coerenza e future estensioni.
     * </p>
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetails> serviceUnavailable(CallNotPermittedException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE,
                AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE,
                AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE,
                request,
                null);
    }

    /**
     * Fallback per eccezioni non previste (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> generic(Exception ex, HttpServletRequest request) {
        log.error("Errore inatteso su {}: {}", safeUri(request), ex.getMessage(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                AppConstants.Problem.TYPE_INTERNAL_ERROR,
                AppConstants.ErrorMessage.ERR_INTERNAL,
                AppConstants.ErrorMessage.MSG_INTERNAL_ERROR,
                request,
                null);
    }

    /**
     * Builder centralizzato per ProblemDetails, per evitare duplicazioni e garantire coerenza.
     */
    private static ResponseEntity<ProblemDetails> build(HttpStatus status,
                                                 String type,
                                                 String title,
                                                 String detail,
                                                 HttpServletRequest request,
                                                 Object extra) {

        ProblemDetails.ProblemDetailsBuilder builder = ProblemDetails.builder()
                .type(type)
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(safeUri(request));

        Optional.ofNullable(extra).ifPresent(builder::extra);

        return ResponseEntity.status(status).body(builder.build());
    }


    /**
     * Estrae in modo safe la request URI.
     */
    private static String safeUri(HttpServletRequest request) {
        return Optional.ofNullable(request)
                .map(HttpServletRequest::getRequestURI)
                .orElse("");
    }

    private static Map<String, String> toFieldErrorExtra(org.springframework.validation.FieldError err) {
        return Map.of(
                AppConstants.ProblemExtra.FIELD, err.getField(),
                AppConstants.ProblemExtra.MESSAGE,
                Optional.ofNullable(err.getDefaultMessage()).orElse(AppConstants.ProblemExtra.DEFAULT_FIELD_ERROR)
        );
    }
}
