package it.sanitech.payments.security;

import it.sanitech.payments.utilities.AppConstants;
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
 * Converter JWT → Authentication.
 *
 * <p>
 * Mappa:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (supporta string o collection)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractScopes(jwt));
        authorities.addAll(extractDepartments(jwt));
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (realmAccess == null) return List.of();

        Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();

        return roles.stream()
                .map(Object::toString)
                .map(r -> r.startsWith(AppConstants.Security.AUTHORITY_PREFIX_ROLE) ? r : AppConstants.Security.AUTHORITY_PREFIX_ROLE + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private Collection<? extends GrantedAuthority> extractScopes(Jwt jwt) {
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope == null || scope.isBlank()) return List.of();

        return Arrays.stream(scope.split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> AppConstants.Security.AUTHORITY_PREFIX_SCOPE + s)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private Collection<? extends GrantedAuthority> extractDepartments(Jwt jwt) {
        Object dept = jwt.getClaims().get(AppConstants.Security.CLAIM_DEPT);
        if (dept == null) return List.of();

        if (dept instanceof String s) {
            if (s.isBlank()) return List.of();
            return List.of(new SimpleGrantedAuthority(AppConstants.Security.AUTHORITY_PREFIX_DEPT + s.toUpperCase()));
        }

        if (dept instanceof Collection<?> depts) {
            return depts.stream()
                    .map(Object::toString)
                    .filter(d -> d != null && !d.isBlank())
                    .map(d -> AppConstants.Security.AUTHORITY_PREFIX_DEPT + d.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        return List.of();
    }
}
