package it.sanitech.directory.exception;

import java.util.Collection;
import java.util.StringJoiner;

/**
 * Eccezione applicativa per indicare che l'utente non ha i permessi per operare
 * su uno o più reparti (HTTP 403).
 *
 * <p>
 * È utilizzata dalle regole ABAC basate su authority {@code DEPT_*}.
 * </p>
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    public DepartmentAccessDeniedException(String message) {
        super(message);
    }

    public static DepartmentAccessDeniedException forDepartment(String dept) {
        String safe = dept == null ? "N/D" : dept;
        return new DepartmentAccessDeniedException("Non sei autorizzato ad operare sul reparto: " + safe + ".");
    }

    public static DepartmentAccessDeniedException forDepartments(Collection<String> depts) {
        StringJoiner sj = new StringJoiner(", ");
        if (depts != null) {
            depts.stream().filter(d -> d != null && !d.isBlank()).forEach(sj::add);
        }
        String safe = sj.toString().isBlank() ? "N/D" : sj.toString();
        return new DepartmentAccessDeniedException("Non sei autorizzato ad operare sui reparti: " + safe + ".");
    }
}
