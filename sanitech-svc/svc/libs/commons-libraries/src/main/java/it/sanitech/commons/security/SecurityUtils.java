package it.sanitech.commons.security;

import it.sanitech.commons.utilities.AppConstants;
import org.springframework.security.core.Authentication;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility di supporto per estrarre informazioni di autorizzazione da {@link Authentication}.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static boolean hasAuthority(Authentication auth, String authority) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), authority));
    }

    public static boolean isAdmin(Authentication auth) {
        return hasRole(auth, AppConstants.Security.ROLE_ADMIN);
    }

    public static boolean isDoctor(Authentication auth) {
        return hasRole(auth, AppConstants.Security.ROLE_DOCTOR);
    }

    public static boolean isPatient(Authentication auth) {
        return hasRole(auth, AppConstants.Security.ROLE_PATIENT);
    }

    public static boolean hasRole(Authentication auth, String role) {
        String roleAuthority = role.startsWith(AppConstants.Security.ROLE_PREFIX)
                ? role
                : AppConstants.Security.ROLE_PREFIX + role;
        return hasAuthority(auth, roleAuthority);
    }

    /**
     * Estrae l'insieme dei reparti autorizzati (authorities {@code DEPT_*}).
     */
    public static Set<String> departmentCodes(Authentication auth) {
        if (auth == null) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a != null && a.startsWith(AppConstants.Security.DEPT_PREFIX))
                .map(a -> a.substring(AppConstants.Security.DEPT_PREFIX.length()))
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
}
