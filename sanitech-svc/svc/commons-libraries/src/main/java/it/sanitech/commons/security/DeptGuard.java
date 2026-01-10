package it.sanitech.commons.security;

import it.sanitech.commons.exception.DepartmentAccessDeniedException;
import it.sanitech.commons.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Guard ABAC (Attribute-Based Access Control) per le policy di reparto.
 *
 * <p>
 * Il mapping delle authority {@code DEPT_*} avviene tramite {@link JwtAuthConverter}.
 * Questo componente centralizza la logica di verifica per ridurre duplicazioni nei service/controller.
 * </p>
 */
@Component
public class DeptGuard {

    /**
     * Verifica se l'utente corrente può operare su un reparto specifico.
     *
     * <p>
     * Regola:
     * <ul>
     *   <li>Se ha {@code ROLE_ADMIN} allora è sempre consentito;</li>
     *   <li>Altrimenti serve l'authority {@code DEPT_<CODICE>}.</li>
     * </ul>
     * </p>
     */
    public boolean canManage(String dept, Authentication auth) {
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.ROLE_PREFIX + "ADMIN"));
        if (isAdmin) return true;

        if (dept == null || dept.isBlank()) return false;

        String needed = AppConstants.Security.DEPT_PREFIX + dept.trim().toUpperCase();
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(needed));
    }

    /**
     * Variante "tutti": utile quando una richiesta contiene più reparti e si vuole richiedere
     * autorizzazione su ciascuno di essi.
     */
    public boolean canManageAll(Collection<String> deptCodes, Authentication auth) {
        if (auth == null) return false;

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.ROLE_PREFIX + "ADMIN"));
        if (isAdmin) return true;

        if (deptCodes == null || deptCodes.isEmpty()) return false;

        Set<String> required = deptCodes.stream()
                .filter(d -> d != null && !d.isBlank())
                .map(d -> AppConstants.Security.DEPT_PREFIX + d.trim().toUpperCase())
                .collect(Collectors.toSet());

        Set<String> current = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return current.containsAll(required);
    }

    /**
     * Estrae i codici reparto dalle authority dell'utente (prefisso {@code DEPT_}).
     *
     * <p>
     * Utile per filtrare dati per reparto (es. un DOCTOR vede solo pazienti del proprio reparto).
     * </p>
     */
    public Set<String> extractDeptCodes(Authentication auth) {
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith(AppConstants.Security.DEPT_PREFIX))
                .map(a -> a.substring(AppConstants.Security.DEPT_PREFIX.length()))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * Helper "fail-fast": lancia un'eccezione semantica se non autorizzato.
     */
    public void checkCanManage(String dept, Authentication auth) {
        if (!canManage(dept, auth)) {
            throw DepartmentAccessDeniedException.forDepartment(dept);
        }
    }

    /**
     * Helper "fail-fast" per richieste che includono più reparti.
     */
    public void checkCanManageAll(Collection<String> deptCodes, Authentication auth) {
        if (!canManageAll(deptCodes, auth)) {
            throw DepartmentAccessDeniedException.forDepartments(deptCodes);
        }
    }
}
