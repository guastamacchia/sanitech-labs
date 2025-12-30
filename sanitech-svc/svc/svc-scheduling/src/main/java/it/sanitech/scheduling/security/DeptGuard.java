package it.sanitech.scheduling.security;

import it.sanitech.scheduling.exception.DepartmentAccessDeniedException;
import it.sanitech.scheduling.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Guard di reparto (ABAC) basato su authority {@code DEPT_*}.
 *
 * <p>
 * Utilizzo tipico: proteggere operazioni amministrative o di gestione che devono
 * essere eseguibili solo per uno specifico reparto (es. creazione slot di un reparto).
 * </p>
 */
@Component
public class DeptGuard {

    /**
     * Verifica se l'utente può gestire il reparto richiesto.
     *
     * @param dept reparto/department code (es. "CARD")
     * @param auth authentication corrente
     * @return {@code true} se autorizzato, {@code false} altrimenti
     */
    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) return false;

        // Admin: sempre autorizzato
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> AppConstants.Security.ROLE_ADMIN.equals(a.getAuthority()));
        if (isAdmin) return true;

        // ABAC: richiesta authority DEPT_<DEPT>
        String needed = AppConstants.Security.PREFIX_DEPT + (dept == null ? "" : dept.toUpperCase());
        return auth.getAuthorities().stream().anyMatch(a -> needed.equals(a.getAuthority()));
    }

    /**
     * Variante “fail-fast”: se non autorizzato solleva un'eccezione applicativa
     * trasformata in RFC 7807 dal {@code GlobalExceptionHandler}.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }
}
