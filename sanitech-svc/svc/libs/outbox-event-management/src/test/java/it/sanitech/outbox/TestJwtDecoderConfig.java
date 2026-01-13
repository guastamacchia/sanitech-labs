package it.sanitech.outbox;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

/**
 * Configurazione di test: JwtDecoder "dummy" per evitare dipendenze da Keycloak/JWK endpoint.
 *
 * <p>
 * Utile per eseguire {@code @SpringBootTest} offline: i test non hanno bisogno di
 * validare realmente token JWT.
 * </p>
 */
@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("scope", "read")
                .claims(c -> c.putAll(Map.of()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
