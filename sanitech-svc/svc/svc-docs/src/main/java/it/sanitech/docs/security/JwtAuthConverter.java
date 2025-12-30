package it.sanitech.docs.security;

import it.sanitech.docs.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converter JWT → Authentication che mappa i claim di Keycloak in authorities Spring Security.
 *
 * <p>
 * Mappature:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} (spazio-separato) → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1) Ruoli Keycloak: realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
            if (rolesObj instanceof Collection<?> roles) {
                authorities.addAll(roles.stream()
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(r -> r.startsWith(AppConstants.Security.AUTH_ROLE_PREFIX) ? r : AppConstants.Security.AUTH_ROLE_PREFIX + r)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));
            }
        }

        // 2) Scope OIDC: "scope" → SCOPE_*
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split("\\s+")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.AUTH_SCOPE_PREFIX + s));
                }
            }
        }

        // 3) Claim custom reparto: "dept" (string oppure array)
        Object deptClaim = jwt.getClaim(AppConstants.Security.CLAIM_DEPT);
        if (deptClaim instanceof String dept) {
            addDeptAuthority(authorities, dept);
        } else if (deptClaim instanceof Collection<?> depts) {
            for (Object d : depts) {
                addDeptAuthority(authorities, Objects.toString(d, ""));
            }
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private void addDeptAuthority(Set<GrantedAuthority> authorities, String dept) {
        if (dept == null) return;
        String normalized = dept.trim();
        if (normalized.isBlank()) return;
        authorities.add(new SimpleGrantedAuthority(AppConstants.Security.AUTH_DEPT_PREFIX + normalized.toUpperCase()));
    }
}
