package it.sanitech.commons.config;

import it.sanitech.commons.security.JwtAuthConverter;
import it.sanitech.commons.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configurazione di sicurezza del microservizio.
 *
 * <p>
 * Note principali:
 * <ul>
 *   <li>Il servizio è un <b>Resource Server JWT</b> (Keycloak/OIDC), senza adapter legacy.</li>
 *   <li>{@link EnableMethodSecurity} abilita le annotazioni come {@code @PreAuthorize} sui metodi.</li>
 *   <li>Gli endpoint tecnici (Swagger/Actuator health) sono pubblici; il resto richiede autenticazione.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AppConstants.Security.PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }
}
