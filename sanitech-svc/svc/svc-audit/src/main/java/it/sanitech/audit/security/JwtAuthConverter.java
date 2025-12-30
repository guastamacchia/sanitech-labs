package it.sanitech.audit.security;

import it.sanitech.audit.utilities.AppConstants;
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
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();

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

        String scope = jwt.getClaimAsString(AppConstants.Security.CLAIM_SCOPE);
        if (scope != null && !scope.isBlank()) {
            for (String s : scope.split("\\s+")) {
                if (!s.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(AppConstants.Security.PREFIX_SCOPE + s.trim()));
                }
            }
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
