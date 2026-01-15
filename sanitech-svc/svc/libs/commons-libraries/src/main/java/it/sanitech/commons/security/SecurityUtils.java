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
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), authority));
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
