package it.sanitech.prescribing.security;

import it.sanitech.prescribing.exception.DepartmentAccessDeniedException;
import it.sanitech.prescribing.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Guard ABAC per verificare l'accesso in base al reparto (department).
 *
 * <p>
 * Convenzione: nel token vengono esposte authorities {@code DEPT_*}.
 * Un utente con {@code ROLE_ADMIN} può operare su qualsiasi reparto.
 * </p>
 */
@Component
public class DeptGuard {

    /**
     * Verifica se l'utente autenticato può operare sul reparto indicato.
     *
     * @param dept reparto richiesto (es. "CARDIO", "HEART")
     * @param auth autenticazione corrente
     * @return true se autorizzato
     */
    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) {
            return false;
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.ROLE_PREFIX + "ADMIN"));
        if (isAdmin) {
            return true;
        }
        String normalized = (dept == null) ? "" : dept.toUpperCase(Locale.ROOT);
        String needed = AppConstants.Security.DEPT_PREFIX + normalized;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(needed));
    }

    /**
     * Variante "fail-fast": se l'utente non è autorizzato, lancia un'eccezione applicativa
     * gestita dal {@code GlobalExceptionHandler} in formato RFC 7807.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }
}
