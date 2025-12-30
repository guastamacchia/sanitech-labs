package it.sanitech.scheduling.security;

import it.sanitech.scheduling.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converter JWT → Authentication per il Resource Server.
 *
 * <p>
 * Converte i claim di Keycloak in authority Spring Security:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC per reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1) realm_access.roles → ROLE_*
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (realmAccess != null && realmAccess.get(AppConstants.Security.CLAIM_ROLES) instanceof Collection<?> roles) {
            authorities.addAll(roles.stream()
                    .map(Object::toString)
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> r.startsWith(AppConstants.Security.PREFIX_ROLE) ? r : AppConstants.Security.PREFIX_ROLE + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }

        // 2) scope → SCOPE_*
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split(" ")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.PREFIX_SCOPE + s));
                }
            }
        }

        // 3) dept → DEPT_* (supporta stringa o lista)
        Object deptClaim = jwt.getClaim(AppConstants.Security.CLAIM_DEPT);
        if (deptClaim instanceof String deptStr) {
            addDeptAuthority(authorities, deptStr);
        } else if (deptClaim instanceof Collection<?> deptList) {
            for (Object d : deptList) {
                addDeptAuthority(authorities, String.valueOf(d));
            }
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private static void addDeptAuthority(Set<GrantedAuthority> authorities, String dept) {
        if (dept == null) return;
        String normalized = dept.trim();
        if (normalized.isBlank()) return;
        authorities.add(new SimpleGrantedAuthority(AppConstants.Security.PREFIX_DEPT + normalized.toUpperCase()));
    }
}
