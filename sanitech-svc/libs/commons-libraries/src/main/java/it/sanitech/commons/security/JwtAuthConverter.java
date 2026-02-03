package it.sanitech.commons.security;

import it.sanitech.commons.utilities.AppConstants;
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
 * Mappa in modo consistente i claim di Keycloak in authority Spring Security:
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
        if (Objects.nonNull(realmAccess) && realmAccess.get(AppConstants.Security.CLAIM_ROLES) instanceof Collection<?> roles) {
            authorities.addAll(
                    roles.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(this::toRoleAuthority)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet())
            );
        }

        // 2) scope → SCOPE_*
        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (Objects.nonNull(scope) && !scope.isBlank()) {
            for (String s : scope.trim().split("\\s+")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.SCOPE_PREFIX + s));
                }
            }
        }

        // 3) dept → DEPT_* (supporta stringa o lista)
        Object deptClaim = jwt.getClaim(AppConstants.Security.CLAIM_DEPT);
        if (deptClaim instanceof String deptStr) {
            addDeptAuthority(authorities, deptStr);
        } else if (deptClaim instanceof Collection<?> deptList) {
            deptList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .forEach(d -> addDeptAuthority(authorities, d));
        }

        // Usa preferred_username (email) come principal name invece di subject (UUID Keycloak)
        String principalName = jwt.getClaimAsString("preferred_username");
        if (principalName == null || principalName.isBlank()) {
            principalName = jwt.getSubject(); // fallback al subject se preferred_username non è presente
        }
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    /**
     * Converte un ruolo in authority con prefisso ROLE_ (se mancante).
     */
    private String toRoleAuthority(String role) {
        return role.startsWith(AppConstants.Security.ROLE_PREFIX)
                ? role
                : AppConstants.Security.ROLE_PREFIX + role;
    }

    /**
     * Aggiunge authority di reparto (DEPT_*) a partire da un valore claim.
     */
    private static void addDeptAuthority(Set<GrantedAuthority> authorities, String dept) {
        if (Objects.isNull(dept)) return;

        String normalized = dept.trim();
        if (normalized.isBlank()) return;

        authorities.add(new SimpleGrantedAuthority(
                AppConstants.Security.DEPT_PREFIX + normalized.toUpperCase(Locale.ROOT)
        ));
    }
}
