package it.sanitech.scheduling.exception;

import it.sanitech.scheduling.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_NOT_FOUND)
                .title(AppConstants.ErrorMessage.ERR_NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Accesso negato per policy di reparto (403).
     */
    @ExceptionHandler(DepartmentAccessDeniedException.class)
    public ResponseEntity<ProblemDetails> deptDenied(DepartmentAccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_ACCESS_DENIED)
                .title(AppConstants.ErrorMessage.ERR_ACCESS_DENIED)
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Errori di validazione (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Dettagli field-level: usati dal frontend per evidenziare i campi invalidi.
        List<Map<String, String>> dettagli = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "messaggio", Optional.ofNullable(err.getDefaultMessage()).orElse(AppConstants.ErrorMessage.MSG_VALIDATION_DEFAULT)
                ))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_VALIDATION_ERROR)
                .title(AppConstants.ErrorMessage.ERR_VALIDATION)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(AppConstants.ErrorMessage.MSG_VALIDATION_FAILED)
                .instance(request.getRequestURI())
                .extra(dettagli)
                .build());
    }

    /**
     * Input non valido (400).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_BAD_REQUEST)
                .title(AppConstants.ErrorMessage.ERR_BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Violazioni vincoli DB (es. unique) → 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_CONFLICT)
                .title(AppConstants.ErrorMessage.ERR_CONFLICT)
                .status(HttpStatus.CONFLICT.value())
                .detail(AppConstants.ErrorMessage.MSG_CONFLICT)
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Rate limiter → troppe richieste (429).
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetails> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_TOO_MANY_REQUESTS)
                .title(AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS)
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .detail(AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS)
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Circuit breaker aperto (503).
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetails> serviceUnavailable(CallNotPermittedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE)
                .title(AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE)
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail(AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE)
                .instance(request.getRequestURI())
                .build());
    }

    /**
     * Fallback per eccezioni non previste (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> generic(Exception ex, HttpServletRequest request) {
        log.error("Errore inatteso su {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_INTERNAL_ERROR)
                .title(AppConstants.ErrorMessage.ERR_INTERNAL)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(AppConstants.ErrorMessage.MSG_INTERNAL_ERROR)
                .instance(request.getRequestURI())
                .build());
    }
}
