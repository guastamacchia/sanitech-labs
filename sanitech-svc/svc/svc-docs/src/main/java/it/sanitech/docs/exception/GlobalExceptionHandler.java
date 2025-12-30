package it.sanitech.docs.exception;

import it.sanitech.docs.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 *
 * <p>
 * Produce risposte in formato RFC 7807 (<em>Problem Details</em>) per uniformare la gestione errori
 * e facilitare l'integrazione dei client.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetails> notFound(NotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, AppConstants.Problem.TYPE_NOT_FOUND, AppConstants.ErrorMessage.ERR_NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Costruiamo un elenco di errori "campo → messaggio" mantenendo un formato semplice per i client.
        List<Map<String, String>> dettagli = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "messaggio", Optional.ofNullable(err.getDefaultMessage()).orElse("Valore non valido")
                ))
                .collect(Collectors.toList());

        return problem(HttpStatus.BAD_REQUEST, AppConstants.Problem.TYPE_VALIDATION_ERROR, AppConstants.ErrorMessage.ERR_VALIDATION,
                "La richiesta contiene errori di validazione.", request, dettagli);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, AppConstants.Problem.TYPE_BAD_REQUEST, AppConstants.ErrorMessage.ERR_BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler({DepartmentAccessDeniedException.class, ConsentDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetails> accessDenied(RuntimeException ex, HttpServletRequest request) {
        String type = (ex instanceof ConsentDeniedException) ? AppConstants.Problem.TYPE_CONSENT_REQUIRED : AppConstants.Problem.TYPE_ACCESS_DENIED;
        String title = (ex instanceof ConsentDeniedException) ? AppConstants.ErrorMessage.ERR_CONSENT_REQUIRED : AppConstants.ErrorMessage.ERR_ACCESS_DENIED;
        String detail = (ex instanceof ConsentDeniedException) ? AppConstants.ErrorMessage.MSG_CONSENT_REQUIRED : AppConstants.ErrorMessage.MSG_ACCESS_DENIED;

        // Per evitare leakage di dettagli sensibili, usiamo un detail standard e mettiamo l'eventuale messaggio specifico in extra.
        Object extra = ex.getMessage();
        return problem(HttpStatus.FORBIDDEN, type, title, detail, request, extra);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetails> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, AppConstants.Problem.TYPE_TOO_MANY_REQUESTS, AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS,
                AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS, request, ex.getMessage());
    }

    @ExceptionHandler({CallNotPermittedException.class, ExternalServiceException.class})
    public ResponseEntity<ProblemDetails> serviceUnavailable(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE, AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE,
                AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE, request, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> generic(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, AppConstants.Problem.TYPE_INTERNAL_ERROR, AppConstants.ErrorMessage.ERR_INTERNAL,
                AppConstants.ErrorMessage.ERR_INTERNAL, request, ex.getMessage());
    }

    private ResponseEntity<ProblemDetails> problem(HttpStatus status,
                                                  String type,
                                                  String title,
                                                  String detail,
                                                  HttpServletRequest request,
                                                  Object extra) {

        ProblemDetails body = ProblemDetails.builder()
                .type(type)
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .extra(extra)
                .build();

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
