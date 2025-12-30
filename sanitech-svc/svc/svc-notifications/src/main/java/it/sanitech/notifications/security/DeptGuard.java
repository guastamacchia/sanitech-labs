package it.sanitech.notifications.security;

import it.sanitech.notifications.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Guard ABAC per operazioni che dipendono dal reparto (claim {@code dept} nel JWT).
 *
 * <p>
 * Regola: un utente è autorizzato se:
 * <ul>
 *   <li>ha {@code ROLE_ADMIN}; oppure</li>
 *   <li>ha l'autorità {@code DEPT_<reparto>}.</li>
 * </ul>
 * </p>
 */
@Component
public class DeptGuard {

    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) {
            return false;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> AppConstants.Security.ROLE_ADMIN.equals(a.getAuthority()));

        if (isAdmin) {
            return true;
        }

        String needed = "DEPT_" + (dept == null ? "" : dept.trim().toUpperCase());
        return auth.getAuthorities().stream().anyMatch(a -> needed.equals(a.getAuthority()));
    }

    /**
     * Variante “fail-fast” utilizzabile nei service layer.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw new DepartmentAccessDeniedException(dept);
        }
    }
}
