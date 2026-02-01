package it.sanitech.gateway.config;

import it.sanitech.gateway.security.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.function.Supplier;

import reactor.core.publisher.Mono;

/**
 * Configurazione di sicurezza del gateway (WebFlux).
 *
 * <p>
 * Il gateway agisce come <strong>Resource Server</strong>:
 * valida il JWT (issuer configurabile) e mappa ruoli/scope/claim custom in {@code GrantedAuthority}
 * tramite {@link JwtAuthConverter}.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        // Adapter necessario perché JwtAuthConverter è sincrono (Converter<Jwt, AbstractAuthenticationToken>).
        Supplier<ReactiveJwtAuthenticationConverterAdapter> adapter =
                () -> new ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        // Preflight CORS (OPTIONS) deve poter passare senza autenticazione.
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger/OpenAPI aggregata e health endpoint sono pubblici (no-auth)
                        .pathMatchers(
                                "/swagger.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/openapi/**",
                                "/actuator/health/**"
                        ).permitAll()
                        // Public API endpoints (patient registration, etc.)
                        .pathMatchers("/api/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(adapter.get()))
                )
                .build();
    }
}
