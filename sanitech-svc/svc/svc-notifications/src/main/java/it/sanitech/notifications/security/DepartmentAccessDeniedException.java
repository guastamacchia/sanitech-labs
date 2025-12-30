package it.sanitech.notifications.security;

import it.sanitech.notifications.exception.ApiProblem;
import it.sanitech.notifications.utilities.AppConstants;

/**
 * Eccezione applicativa per accesso negato a risorse vincolate a un reparto (ABAC).
 *
 * <p>Viene tradotta in RFC 7807 dal {@code GlobalExceptionHandler}.</p>
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    private final String department;

    public DepartmentAccessDeniedException(String department) {
        super("Accesso negato per il reparto: " + department);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    /**
     * Crea un payload Problem Details standardizzato per questa eccezione.
     */
    public ApiProblem toProblem(String instance) {
        return ApiProblem.builder()
                .type(AppConstants.Problem.TYPE_FORBIDDEN)
                .title(AppConstants.ErrorMessage.ERR_FORBIDDEN)
                .status(403)
                .detail(getMessage())
                .instance(instance)
                .build();
    }
}
