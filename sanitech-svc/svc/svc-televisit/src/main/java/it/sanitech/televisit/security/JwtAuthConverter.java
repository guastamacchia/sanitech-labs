package it.sanitech.televisit.security;

import it.sanitech.televisit.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converte un JWT OIDC in un {@link JwtAuthenticationToken} arricchendo le authorities.
 *
 * <p>Mappature supportate:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} (stringa space-separated) → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        addRealmRoles(jwt, authorities);
        addScopes(jwt, authorities);
        addDepartment(jwt, authorities);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private void addRealmRoles(Jwt jwt, Set<GrantedAuthority> target) {
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (realmAccess == null) {
            return;
        }
        Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) {
            return;
        }
        for (Object r : roles) {
            String role = Objects.toString(r, "").trim();
            if (role.isEmpty()) continue;

            String normalized = role.startsWith(AppConstants.Security.ROLE_PREFIX)
                    ? role
                    : AppConstants.Security.ROLE_PREFIX + role;

            target.add(new SimpleGrantedAuthority(normalized));
        }
    }

    private void addScopes(Jwt jwt, Set<GrantedAuthority> target) {
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope == null || scope.isBlank()) {
            return;
        }
        for (String s : scope.split(" ")) {
            String val = s.trim();
            if (!val.isEmpty()) {
                target.add(new SimpleGrantedAuthority(AppConstants.Security.SCOPE_PREFIX + val));
            }
        }
    }

    private void addDepartment(Jwt jwt, Set<GrantedAuthority> target) {
        String dept = jwt.getClaimAsString(AppConstants.Security.CLAIM_DEPT);
        if (dept == null || dept.isBlank()) {
            return;
        }
        target.add(new SimpleGrantedAuthority(AppConstants.Security.DEPT_PREFIX + dept.trim().toUpperCase()));
    }
}
