package it.sanitech.admissions.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import it.sanitech.admissions.utilities.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gestore globale delle eccezioni.
 *
 * <p>
 * Converte le eccezioni applicative e infrastrutturali in risposte standard RFC 7807
 * (Problem Details), mantenendo messaggi in italiano e struttura uniforme.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<ApiProblem> problem(HttpStatus status, ApiProblem body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiProblem> notFound(NotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_NOT_FOUND)
                .title(AppConstants.ErrorMessage.ERR_NOT_FOUND)
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiProblem> conflict(ConflictException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_CONFLICT)
                .title(AppConstants.ErrorMessage.ERR_CONFLICT)
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(NoBedAvailableException.class)
    public ResponseEntity<ApiProblem> noBeds(NoBedAvailableException ex, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_NO_BEDS)
                .title(AppConstants.ErrorMessage.ERR_NO_BEDS)
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(DepartmentAccessDeniedException.class)
    public ResponseEntity<ApiProblem> deptDenied(DepartmentAccessDeniedException ex, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_ACCESS_DENIED)
                .title(AppConstants.ErrorMessage.ERR_ACCESS_DENIED)
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiProblem> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> dettagli = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "campo", err.getField(),
                        "messaggio", Optional.ofNullable(err.getDefaultMessage()).orElse("Valore non valido")
                ))
                .collect(Collectors.toList());

        return problem(HttpStatus.BAD_REQUEST, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_VALIDATION_ERROR)
                .title(AppConstants.ErrorMessage.ERR_VALIDATION)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(AppConstants.ErrorMessage.MSG_VALIDATION_FAILED)
                .instance(request.getRequestURI())
                .extra(dettagli)
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiProblem> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_BAD_REQUEST)
                .title(AppConstants.ErrorMessage.ERR_BAD_REQUEST)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiProblem> tooManyRequests(RequestNotPermitted ex, HttpServletRequest request) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_TOO_MANY_REQUESTS)
                .title(AppConstants.ErrorMessage.ERR_TOO_MANY_REQUESTS)
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .detail(AppConstants.ErrorMessage.MSG_TOO_MANY_REQUESTS)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiProblem> serviceUnavailable(CallNotPermittedException ex, HttpServletRequest request) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_SERVICE_UNAVAILABLE)
                .title(AppConstants.ErrorMessage.ERR_SERVICE_UNAVAILABLE)
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .detail(AppConstants.ErrorMessage.MSG_SERVICE_UNAVAILABLE)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> generic(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_INTERNAL_ERROR)
                .title(AppConstants.ErrorMessage.ERR_INTERNAL)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(AppConstants.ErrorMessage.MSG_INTERNAL_ERROR)
                .instance(request.getRequestURI())
                .extra(ex.getMessage())
                .build());
    }
}
