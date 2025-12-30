package it.sanitech.admissions.security;

import it.sanitech.admissions.exception.DepartmentAccessDeniedException;
import it.sanitech.admissions.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Guard di autorizzazione (ABAC) basato sul reparto.
 *
 * <p>
 * Regola:
 * <ul>
 *   <li>Se l'utente ha {@code ROLE_ADMIN} → consentito.</li>
 *   <li>Altrimenti serve l'autorità {@code DEPT_<REPARTO>} coerente con il reparto richiesto.</li>
 * </ul>
 * </p>
 */
@Component
public class DeptGuard {

    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), AppConstants.Security.ROLE_ADMIN));

        if (isAdmin) return true;

        if (dept == null || dept.isBlank()) return false;

        String needed = AppConstants.Security.AUTH_PREFIX_DEPT + dept.toUpperCase();

        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), needed));
    }

    /**
     * Variante "fail-fast": solleva eccezione applicativa se l'utente non è autorizzato.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }
}
