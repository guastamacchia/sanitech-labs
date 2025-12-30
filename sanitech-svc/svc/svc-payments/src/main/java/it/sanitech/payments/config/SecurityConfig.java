package it.sanitech.payments.config;

import it.sanitech.payments.security.JwtAuthConverter;
import it.sanitech.payments.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration (Resource Server JWT).
 *
 * <p>
 * {@code @EnableMethodSecurity} abilita le annotazioni {@code @PreAuthorize/@PostAuthorize}
 * nei controller/service, in modo da applicare policy di autorizzazione espressive e testabili.
 * </p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API stateless: CSRF non necessario
                .csrf(AbstractHttpConfigurer::disable)
                // CORS da CorsConfigurationSource
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AppConstants.Security.PUBLIC_ENDPOINTS).permitAll()
                        // Webhook: protetto da secret applicativo, ma richiede comunque una chain
                        .requestMatchers(AppConstants.Api.WEBHOOK_BASE + "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        return http.build();
    }
}
