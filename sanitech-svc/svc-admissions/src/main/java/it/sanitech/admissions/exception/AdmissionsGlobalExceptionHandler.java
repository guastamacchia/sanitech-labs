package it.sanitech.admissions.exception;

import it.sanitech.admissions.utilities.AppConstants;
import it.sanitech.commons.exception.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestore delle eccezioni specifiche per i ricoveri.
 */
@RestControllerAdvice
public class AdmissionsGlobalExceptionHandler {

    @ExceptionHandler(NoBedAvailableException.class)
    public ResponseEntity<ProblemDetails> noBeds(NoBedAvailableException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetails.builder()
                .type(AppConstants.Problem.TYPE_NO_BEDS)
                .title(AppConstants.ErrorMessage.ERR_NO_BEDS)
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build());
    }
}
