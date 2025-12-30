package it.sanitech.docs.security;

import it.sanitech.docs.exception.DepartmentAccessDeniedException;
import it.sanitech.docs.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Guard ABAC (Attribute-Based Access Control) basato sul reparto.
 *
 * <p>
 * Regola: un utente può gestire (o accedere) risorse di un reparto se:
 * <ul>
 *   <li>ha il ruolo {@code ROLE_ADMIN}, oppure</li>
 *   <li>possiede un'autorità {@code DEPT_<reparto>} coerente con il reparto richiesto.</li>
 * </ul>
 * </p>
 */
@Component
public class DeptGuard {

    /**
     * Verifica se l'utente autenticato può operare sul reparto indicato.
     *
     * @param dept reparto della risorsa (es. {@code CARDIO})
     * @param auth autenticazione corrente
     * @return {@code true} se autorizzato, {@code false} altrimenti
     */
    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.AUTH_ROLE_PREFIX + "ADMIN"));
        if (isAdmin) return true;

        if (dept == null || dept.isBlank()) return false;

        String needed = AppConstants.Security.AUTH_DEPT_PREFIX + dept.trim().toUpperCase();
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(needed));
    }

    /**
     * Variante "fail-fast": se l'utente non è autorizzato, lancia un'eccezione applicativa.
     *
     * @param dept reparto richiesto
     * @param auth autenticazione corrente
     * @throws DepartmentAccessDeniedException se non autorizzato
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }
}
