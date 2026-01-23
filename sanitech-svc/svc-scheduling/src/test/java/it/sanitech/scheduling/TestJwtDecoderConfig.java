package it.sanitech.scheduling;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configurazione di test: evita dipendenze esterne (Keycloak) fornendo un decoder JWT fittizio.
 *
 * <p>
 * In produzione il decoder viene configurato automaticamente tramite {@code issuer-uri}.
 * </p>
 */
@TestConfiguration
public class TestJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        // decoder “dummy”: non usiamo la validazione JWK nei test d'integrazione.
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:0/dummy").build();
    }
}
