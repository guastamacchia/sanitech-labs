package it.sanitech.commons.exception;

import it.sanitech.commons.utilities.AppConstants;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Eccezione applicativa per indicare che l'utente non ha i permessi per operare
 * su uno o più reparti (HTTP 403).
 *
 * <p>
 * È utilizzata dalle regole ABAC basate su authority {@code DEPT_*}.
 * </p>
 *
 * <p>
 * Policy:
 * <ul>
 *   <li>fail-safe: input nullo o vuoto → messaggio con valore di fallback</li>
 *   <li>normalizzazione: trim e rimozione valori blank</li>
 * </ul>
 * </p>
 */
public class DepartmentAccessDeniedException extends RuntimeException {

    // messaggi specifici ABAC reparto
    private static final String MSG_DEPT_FORBIDDEN_SINGLE = "Non sei autorizzato ad operare sul reparto %s.";
    private static final String MSG_DEPT_FORBIDDEN_MULTI = "Non sei autorizzato ad operare sui reparti %s.";

    private DepartmentAccessDeniedException(String message) {
        super(message);
    }

    /**
     * Factory per singolo reparto.
     */
    public static DepartmentAccessDeniedException forDepartment(String dept) {
        String safeDept = normalizeDept(dept);

        return new DepartmentAccessDeniedException(String.format(MSG_DEPT_FORBIDDEN_SINGLE, safeDept));
    }

    /**
     * Factory per più reparti.
     */
    public static DepartmentAccessDeniedException forDepartments(Collection<String> depts) {
        String safeDepts = normalizeDepts(depts);

        return new DepartmentAccessDeniedException(String.format(MSG_DEPT_FORBIDDEN_MULTI, safeDepts));
    }

    /**
     * Normalizza un singolo codice reparto.
     */
    private static String normalizeDept(String dept) {
        if (Objects.isNull(dept) || dept.isBlank()) {
            return AppConstants.ErrorMessage.FALLBACK_VALUE;
        }
        return dept.trim();
    }

    /**
     * Normalizza una collection di codici reparto in stringa leggibile.
     */
    private static String normalizeDepts(Collection<String> depts) {
        if (Objects.isNull(depts) || depts.isEmpty()) {
            return AppConstants.ErrorMessage.FALLBACK_VALUE;
        }

        String joined = depts.stream()
                .filter(d -> Objects.nonNull(d) && !d.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));

        return joined.isBlank() ? AppConstants.ErrorMessage.FALLBACK_VALUE : joined;
    }
}
