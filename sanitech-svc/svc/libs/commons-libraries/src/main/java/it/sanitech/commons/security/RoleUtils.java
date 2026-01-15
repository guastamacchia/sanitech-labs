package it.sanitech.commons.security;

import it.sanitech.commons.utilities.AppConstants;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;

@UtilityClass
public class RoleUtils {

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
        if (Objects.isNull(auth) || Objects.isNull(auth.getAuthorities())) {
            return false;
        }
        String roleAuthority = role.startsWith(AppConstants.Security.ROLE_PREFIX)
                ? role
                : AppConstants.Security.ROLE_PREFIX + role;
        return auth.getAuthorities().stream()
                .anyMatch(a -> roleAuthority.equals(a.getAuthority()));
    }
}
