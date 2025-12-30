package it.sanitech.notifications.exception;

import it.sanitech.notifications.security.DepartmentAccessDeniedException;
import it.sanitech.notifications.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
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
 * Produce risposte in formato RFC 7807 (Problem Details) per garantire un formato uniforme,
 * facilmente consumabile da UI e sistemi di observability.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiProblem> notFound(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_NOT_FOUND)
                .title(AppConstants.ErrorMessage.ERR_NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiProblem> badRequest(BadRequestException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_BAD_REQUEST)
                .title(AppConstants.ErrorMessage.ERR_BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiProblem> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                // Per ogni field error estraiamo il nome campo e il messaggio già “user friendly”
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "messaggio", Optional.ofNullable(err.getDefaultMessage()).orElse("Valore non valido")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_VALIDATION)
                .title(AppConstants.ErrorMessage.ERR_VALIDATION)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(AppConstants.ErrorMessage.MSG_VALIDATION_FAILED)
                .instance(request.getRequestURI())
                .errors(errors)
                .build());
    }

    @ExceptionHandler(DepartmentAccessDeniedException.class)
    public ResponseEntity<ApiProblem> forbidden(DepartmentAccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.toProblem(request.getRequestURI()));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiProblem> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_TOO_MANY_REQUESTS)
                .title(AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS)
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .detail(AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS)
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiProblem> serviceUnavailable(CallNotPermittedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE)
                .title(AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE)
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail(AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE)
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> generic(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_INTERNAL_ERROR)
                .title(AppConstants.ErrorMessage.ERR_INTERNAL)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(AppConstants.ErrorMessage.MSG_INTERNAL_ERROR)
                .instance(request.getRequestURI())
                .errors(Map.of("exception", ex.getClass().getSimpleName()))
                .build());
    }
}
