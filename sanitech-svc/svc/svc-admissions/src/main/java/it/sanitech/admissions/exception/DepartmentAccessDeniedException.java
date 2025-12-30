package it.sanitech.admissions.exception;

/**
 * Eccezione applicativa per negare l'accesso quando l'utente non è autorizzato
 * a operare su uno specifico reparto (ABAC).
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    public DepartmentAccessDeniedException(String message) {
        super(message);
    }

    public static DepartmentAccessDeniedException forDepartment(String dept) {
        return new DepartmentAccessDeniedException("Non sei autorizzato a operare sul reparto: " + dept);
    }
}
