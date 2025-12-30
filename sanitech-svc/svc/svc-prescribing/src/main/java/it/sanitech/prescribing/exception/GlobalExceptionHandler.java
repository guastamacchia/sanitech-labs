package it.sanitech.prescribing.exception;

import it.sanitech.prescribing.utilities.AppConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/**
 * Gestione centralizzata delle eccezioni applicative in formato RFC 7807.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, AppConstants.Problem.TYPE_NOT_FOUND, AppConstants.ErrorMessage.ERR_NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetails> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, AppConstants.Problem.TYPE_BAD_REQUEST, AppConstants.ErrorMessage.ERR_BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        Map<String, Object> extra = Map.of(AppConstants.JsonKeys.ERRORS, errors);

        return build(HttpStatus.BAD_REQUEST, AppConstants.Problem.TYPE_VALIDATION, AppConstants.ErrorMessage.ERR_VALIDATION,
                AppConstants.ErrorMessage.MSG_VALIDATION_FAILED, req.getRequestURI(), extra);
    }

    @ExceptionHandler({DepartmentAccessDeniedException.class, ConsentDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetails> handleForbidden(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, AppConstants.Problem.TYPE_FORBIDDEN, AppConstants.ErrorMessage.ERR_FORBIDDEN, ex.getMessage(), req.getRequestURI(), null);
    }

    @ExceptionHandler({ExternalServiceException.class, CallNotPermittedException.class})
    public ResponseEntity<ProblemDetails> handleServiceUnavailable(Exception ex, HttpServletRequest req) {
        String detail = (ex instanceof ExternalServiceException ese) ? ese.getMessage() : AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE;
        return build(HttpStatus.SERVICE_UNAVAILABLE, AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE, AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE, detail, req.getRequestURI(), null);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ProblemDetails> handleRateLimit(RequestNotPermitted ex, HttpServletRequest req) {
        return build(HttpStatus.TOO_MANY_REQUESTS, AppConstants.Problem.TYPE_TOO_MANY_REQUESTS, AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS,
                AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS, req.getRequestURI(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, AppConstants.Problem.TYPE_INTERNAL_ERROR, AppConstants.ErrorMessage.ERR_INTERNAL,
                AppConstants.ErrorMessage.MSG_INTERNAL_ERROR, req.getRequestURI(), null);
    }

    private Map<String, Object> toFieldError(FieldError fe) {
        return Map.of(
                AppConstants.JsonKeys.FIELD, fe.getField(),
                AppConstants.JsonKeys.MESSAGE, (fe.getDefaultMessage() != null) ? fe.getDefaultMessage() : "Valore non valido"
        );
    }

    private ResponseEntity<ProblemDetails> build(HttpStatus status, String type, String title, String detail, String instance, Map<String, Object> extra) {
        ProblemDetails body = ProblemDetails.builder()
                .type(type)
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(instance)
                .extra(extra)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
