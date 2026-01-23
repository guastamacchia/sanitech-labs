package it.sanitech.prescribing;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

/**
 * Config test: fornisce un {@link JwtDecoder} minimale per soddisfare il contesto Spring Security.
 *
 * <p>
 * I test possono bypassare i filtri con {@code @AutoConfigureMockMvc(addFilters = false)}; tuttavia
 * il bean è comunque utile per evitare errori di bootstrap del contesto.
 * </p>
 */
@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test")
                .claim("scope", "prescriptions.read prescriptions.write")
                .claim("pid", 1001)
                .claim("did", 2001)
                .claim("dept", "CARDIOLOGY")
                .claim("realm_access", Map.of("roles", java.util.List.of("ADMIN", "DOCTOR", "PATIENT")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
