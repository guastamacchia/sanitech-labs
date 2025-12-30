package it.sanitech.televisit.security;

import it.sanitech.televisit.exception.DepartmentAccessDeniedException;
import it.sanitech.televisit.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Guard ABAC (Attribute-Based Access Control) per reparto.
 *
 * <p>Regole:
 * <ul>
 *   <li>Se l'utente ha {@code ROLE_ADMIN} può operare su qualsiasi reparto.</li>
 *   <li>Altrimenti deve possedere l'autorità {@code DEPT_<DEPARTMENT>}.</li>
 * </ul>
 * </p>
 */
@Component
public class DeptGuard {

    public boolean canManage(String dept, Authentication auth) {
        if (auth == null || dept == null || dept.isBlank()) {
            return false;
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> AppConstants.Security.ROLE_ADMIN.equals(a.getAuthority()));
        if (isAdmin) {
            return true;
        }

        String needed = AppConstants.Security.DEPT_PREFIX + dept.trim().toUpperCase();
        return auth.getAuthorities().stream().anyMatch(a -> needed.equals(a.getAuthority()));
    }

    /**
     * Variante “fail-fast”: se l'utente non può gestire il reparto, lancia un'eccezione applicativa.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }
}
