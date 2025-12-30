package it.sanitech.consents.security;

import it.sanitech.consents.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converte un {@link Jwt} in un {@link AbstractAuthenticationToken} arricchendo le authority.
 * <p>
 * Mapping applicato:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*}</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

        // 1) Realm roles (Keycloak)
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
            if (rolesObj instanceof Collection<?> roles) {
                for (Object r : roles) {
                    String role = Objects.toString(r, "").trim();
                    if (!role.isEmpty()) {
                        String normalized = role.startsWith(AppConstants.Security.PREFIX_ROLE)
                                ? role
                                : AppConstants.Security.PREFIX_ROLE + role;
                        authorities.add(new SimpleGrantedAuthority(normalized));
                    }
                }
            }
        }

        // 2) OAuth2 scopes
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split("\\s+")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.PREFIX_SCOPE + s.trim()));
                }
            }
        }

        // 3) Dipartimento (ABAC)
        String dept = jwt.getClaimAsString(AppConstants.Security.CLAIM_DEPT);
        if (dept != null && !dept.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(AppConstants.Security.PREFIX_DEPT + dept.trim().toUpperCase()));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
