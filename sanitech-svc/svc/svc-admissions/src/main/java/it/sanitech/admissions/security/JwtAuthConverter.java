package it.sanitech.admissions.security;

import it.sanitech.admissions.utilities.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converte un {@link Jwt} in un {@link AbstractAuthenticationToken} arricchendo le authorities.
 *
 * <p>
 * Mapping implementato:
 * <ul>
 *   <li>{@code realm_access.roles[]} → {@code ROLE_*}</li>
 *   <li>{@code scope} (stringa spazio-separata) → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} (stringa o lista) → {@code DEPT_*} (ABAC per reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();

        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractScopes(jwt));
        authorities.addAll(extractDepartments(jwt));

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Object realmAccessObj = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }

        Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(r -> r.startsWith(AppConstants.Security.AUTH_PREFIX_ROLE) ? r : AppConstants.Security.AUTH_PREFIX_ROLE + r)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private Collection<? extends GrantedAuthority> extractScopes(Jwt jwt) {
        String scope = firstNonBlank(
                jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE),
                jwt.getClaimAsString("scp") // fallback: alcuni provider usano "scp"
        );

        if (scope == null) {
            return List.of();
        }

        String[] parts = scope.trim().split("\\s+");
        List<GrantedAuthority> authorities = new ArrayList<>(parts.length);

        for (String s : parts) {
            if (!s.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(AppConstants.Security.AUTH_PREFIX_SCOPE + s));
            }
        }
        return authorities;
    }

    private Collection<? extends GrantedAuthority> extractDepartments(Jwt jwt) {
        Object deptObj = jwt.getClaim(AppConstants.Security.CLAIM_DEPT);
        if (deptObj == null) {
            return List.of();
        }

        // Supporta sia stringa singola sia lista (più reparti).
        Collection<String> depts = new ArrayList<>();

        if (deptObj instanceof String deptStr) {
            addIfPresent(depts, deptStr);
        } else if (deptObj instanceof Collection<?> deptList) {
            for (Object d : deptList) {
                if (d != null) {
                    addIfPresent(depts, d.toString());
                }
            }
        }

        return depts.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .map(d -> new SimpleGrantedAuthority(AppConstants.Security.AUTH_PREFIX_DEPT + d))
                .toList();
    }

    private static void addIfPresent(Collection<String> target, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            target.add(value);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
