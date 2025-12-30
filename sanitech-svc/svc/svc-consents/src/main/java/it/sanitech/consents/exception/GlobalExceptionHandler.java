package it.sanitech.consents.exception;

import it.sanitech.consents.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gestore globale delle eccezioni del microservizio.
 * <p>
 * Produce risposte in formato RFC 7807 (Problem Details), garantendo un formato uniforme
 * e standardizzato per tutte le classi di errore.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetails> conflict(ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_CONFLICT)
                .title(AppConstants.ErrorMessage.ERR_CONFLICT)
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler({ForbiddenOperationException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetails> forbidden(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_FORBIDDEN)
                .title(AppConstants.ErrorMessage.ERR_FORBIDDEN)
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Dettagli campo-per-campo (utile lato UI e per debug).
        List<Map<String, String>> dettagli = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "messaggio", Optional.ofNullable(err.getDefaultMessage()).orElse("Valore non valido")
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> illegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_BAD_REQUEST)
                .title(AppConstants.ErrorMessage.ERR_VALIDATION)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetails> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_TOO_MANY_REQUESTS)
                .title(AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS)
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .detail(AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetails> serviceUnavailable(CallNotPermittedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE)
                .title(AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE)
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail(AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> generic(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_INTERNAL_ERROR)
                .title(AppConstants.ErrorMessage.ERR_INTERNAL)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(AppConstants.ErrorMessage.MSG_INTERNAL)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }
}
