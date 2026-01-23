package it.sanitech.gateway.security;

import it.sanitech.gateway.utilities.AppConstants;
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
 * Converter dedicato per trasformare un {@link Jwt} in un {@link AbstractAuthenticationToken}
 * con {@link GrantedAuthority} coerenti con gli standard della piattaforma Sanitech.
 *
 * <p>
 * Mapping applicato:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} (spaziato) → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC per reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>();

        // 1) Ruoli di realm (Keycloak): realm_access.roles
        extractRealmRoles(jwt).forEach(role ->
                authorities.add(new SimpleGrantedAuthority(normalizeRole(role)))
        );

        // 2) Scope OIDC: claim "scope" (es. "openid profile email")
        extractScopes(jwt).forEach(scope ->
                authorities.add(new SimpleGrantedAuthority(AppConstants.Security.AUTHORITY_PREFIX_SCOPE + scope))
        );

        // 3) ABAC per reparto (custom claim): dept (string o array)
        extractDepartments(jwt).forEach(dept ->
                authorities.add(new SimpleGrantedAuthority(AppConstants.Security.AUTHORITY_PREFIX_DEPT + dept.toUpperCase(Locale.ROOT)))
        );

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return AppConstants.Security.AUTHORITY_PREFIX_ROLE + "UNKNOWN";
        }
        return role.startsWith(AppConstants.Security.AUTHORITY_PREFIX_ROLE)
                ? role
                : AppConstants.Security.AUTHORITY_PREFIX_ROLE + role;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractRealmRoles(Jwt jwt) {
        Object realmAccessObj = jwt.getClaim(AppConstants.Security.CLAIM_REALM_ACCESS);
        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return Set.of();
        }
        Object rolesObj = realmAccess.get(AppConstants.Security.CLAIM_ROLES);
        if (!(rolesObj instanceof Collection<?> roles)) {
            return Set.of();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<String> extractScopes(Jwt jwt) {
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope == null || scope.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scope.split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<String> extractDepartments(Jwt jwt) {
        Object deptObj = jwt.getClaim(AppConstants.Security.CLAIM_DEPT);
        if (deptObj == null) {
            return Set.of();
        }

        // dept può essere stringa (es. "CARDIO") oppure array (es. ["CARDIO","METAB"])
        if (deptObj instanceof String s) {
            if (s.isBlank()) return Set.of();
            // Supporto anche stringa CSV (in caso di claim “legacy”)
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (deptObj instanceof Collection<?> c) {
            return c.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(v -> !v.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Set.of();
    }
}
