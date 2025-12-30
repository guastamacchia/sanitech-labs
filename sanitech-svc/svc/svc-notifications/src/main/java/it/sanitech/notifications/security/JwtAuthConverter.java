package it.sanitech.notifications.security;

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
 * Converte un {@link Jwt} in un token Spring Security con authorities coerenti.
 *
 * <p>
 * Mapping supportato:
 * <ul>
 *   <li>{@code realm_access.roles} → {@code ROLE_*}</li>
 *   <li>{@code scope} → {@code SCOPE_*}</li>
 *   <li>claim custom {@code dept} → {@code DEPT_*} (ABAC reparto)</li>
 * </ul>
 * </p>
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Roles (Keycloak)
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            authorities.addAll(roles.stream()
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(r -> !r.isBlank())
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }

        // Scopes
        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split(" ")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + s.trim()));
                }
            }
        }

        // Department ABAC
        String dept = jwt.getClaimAsString("dept");
        if (dept != null && !dept.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("DEPT_" + dept.trim().toUpperCase()));
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
