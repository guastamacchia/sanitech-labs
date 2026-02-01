package it.sanitech.commons.security;

import it.sanitech.commons.exception.DepartmentAccessDeniedException;
import it.sanitech.commons.utilities.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Guard ABAC (Attribute-Based Access Control) per le policy di reparto.
 *
 * <p>
 * Il mapping delle authority {@code DEPT_*} avviene tramite {@link JwtAuthConverter}.
 * </p>
 *
 * <p>
 * Policy "fail-closed": in caso di input non valido (dept nullo/vuoto o lista priva di codici validi)
 * l'accesso viene negato.
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
        if (Objects.isNull(auth)) return false;

        if (isAdmin(auth)) return true;

        String normalized = normalizeDeptCode(dept);
        if (Objects.isNull(normalized)) return false;

        String needed = AppConstants.Security.DEPT_PREFIX + normalized;
        return authorities(auth).contains(needed);
    }

    /**
     * Variante "tutti": utile quando una richiesta contiene più reparti e si vuole richiedere
     * autorizzazione su ciascuno di essi.
     *
     * <p>
     * Importante: se la collection non contiene alcun codice valido (solo null/vuoti),
     * la policy è "fail-closed" e ritorna false.
     * </p>
     */
    public boolean canManageAll(Collection<String> deptCodes, Authentication auth) {
        if (Objects.isNull(auth)) return false;

        if (isAdmin(auth)) return true;

        if (Objects.isNull(deptCodes) || deptCodes.isEmpty()) return false;

        Set<String> required = deptCodes.stream()
                .map(DeptGuard::normalizeDeptCode)
                .filter(Objects::nonNull)
                .map(code -> AppConstants.Security.DEPT_PREFIX + code)
                .collect(Collectors.toSet());

        // Fail-closed: nessun codice valido -> non autorizzare.
        if (required.isEmpty()) return false;

        Set<String> current = authorities(auth);
        return current.containsAll(required);
    }

    /**
     * Estrae i codici reparto dalle authority dell'utente (prefisso {@code DEPT_}).
     */
    public Set<String> extractDeptCodes(Authentication auth) {
        if (Objects.isNull(auth)) return Set.of();

        return authorities(auth).stream()
                .filter(a -> a.startsWith(AppConstants.Security.DEPT_PREFIX))
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

    /**
     * Determina se l'utente ha il ruolo amministratore.
     *
     * <p>
     * Separato per:
     * - ridurre duplicazione
     * - mantenere allineata la semantica tra i metodi di guard
     * </p>
     */
    private static boolean isAdmin(Authentication auth) {
        return authorities(auth).contains(AppConstants.Security.ROLE_ADMIN);
    }

    /**
     * Normalizza un codice reparto:
     * - null/vuoto -> null
     * - trim
     * - uppercase (Locale.ROOT)
     */
    private static String normalizeDeptCode(String dept) {
        if (Objects.isNull(dept) || dept.isBlank()) return null;
        return dept.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Estrae le authority dell'utente in forma di set (senza null).
     */
    private static Set<String> authorities(Authentication auth) {
        if (Objects.isNull(auth) || Objects.isNull(auth.getAuthorities())) return Set.of();
        return auth.getAuthorities().stream()
                .filter(a -> Objects.nonNull(a) && Objects.nonNull(a.getAuthority()))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
