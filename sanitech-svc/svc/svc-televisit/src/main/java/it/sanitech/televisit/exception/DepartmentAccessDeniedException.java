package it.sanitech.televisit.exception;

/**
 * Eccezione applicativa per accesso negato su reparto (ABAC).
 *
 * <p>Viene resa come HTTP 403 (RFC 7807) dal {@link GlobalExceptionHandler}.</p>
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    private DepartmentAccessDeniedException(String message) {
        super(message);
    }

    public static DepartmentAccessDeniedException forDepartment(String dept) {
        String d = (dept == null || dept.isBlank()) ? "N/D" : dept.trim().toUpperCase();
        return new DepartmentAccessDeniedException("Non hai i permessi per operare sul reparto: " + d + ".");
    }
}
