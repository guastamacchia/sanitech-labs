package it.sanitech.prescribing.security;

import it.sanitech.prescribing.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converter custom per trasformare un {@link Jwt} in una {@link AuthenticationToken}
 * con authorities compatibili con {@code @PreAuthorize}.
 *
 * <p>Regole di mapping:</p>
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} (spaziato) → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC di reparto)</li>
 * </ul>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 1) Ruoli Keycloak (realm roles)
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.JwtClaim.REALM_ACCESS);
        Object rolesObj = (realmAccess != null) ? realmAccess.get(AppConstants.JwtClaim.ROLES) : null;
        if (rolesObj instanceof Collection<?> roles) {
            authorities.addAll(
                    roles.stream()
                            .map(Object::toString)
                            .filter(s -> !s.isBlank())
                            .map(this::normalizeRole)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet())
            );
        }

        // 2) Scope OAuth2
        String scope = jwt.getClaimAsString(AppConstants.JwtClaim.SCOPE);
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split(" ")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.SCOPE_PREFIX + s));
                }
            }
        }

        // 3) ABAC reparto: supportiamo sia stringa singola che array/lista
        Object deptClaim = jwt.getClaim(AppConstants.JwtClaim.DEPT);
        if (deptClaim instanceof String dept && !dept.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(AppConstants.Security.DEPT_PREFIX + dept.toUpperCase(Locale.ROOT)));
        } else if (deptClaim instanceof Collection<?> depts) {
            depts.stream()
                    .map(Object::toString)
                    .filter(s -> !s.isBlank())
                    .map(s -> AppConstants.Security.DEPT_PREFIX + s.toUpperCase(Locale.ROOT))
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private String normalizeRole(String role) {
        return role.startsWith(AppConstants.Security.ROLE_PREFIX)
                ? role
                : AppConstants.Security.ROLE_PREFIX + role;
    }
}
