package it.sanitech.commons.config;

import it.sanitech.commons.security.JwtAuthConverter;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Configurazione di sicurezza del microservizio (Spring Security 6).
 *
 * <p>
 * Principi adottati:
 * </p>
 * <ul>
 *   <li>Il servizio è un Resource Server JWT (OIDC/Keycloak).</li>
 *   <li>Sessione stateless: ogni richiesta deve includere il token.</li>
 *   <li>Gli endpoint tecnici configurati in {@code AppConstants.Security.PUBLIC_ENDPOINTS} sono pubblici.</li>
 *   <li>Tutto il resto richiede autenticazione; autorizzazioni fine-grained via {@code @PreAuthorize}.</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * Validazione e diagnostica della configurazione a startup.
     *
     * <p>
     * Scopo: rendere visibile la configurazione effettiva e intercettare errori banali
     * (es. lista endpoint pubblici vuota o converter non disponibile).
     * </p>
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("Sicurezza: avvio validazione configurazione Spring Security.");

        String[] publicEndpoints = AppConstants.Security.PUBLIC_ENDPOINTS;

        log.debug("Sicurezza: policy sessione prevista: STATELESS.");
        log.debug("Sicurezza: CORS abilitato (policy definita nella configurazione CORS).");
        log.debug("Sicurezza: endpoint pubblici configurati: {}.",
                publicEndpoints == null ? "null" : Arrays.toString(publicEndpoints));

        if (jwtAuthConverter == null) {
            log.error("Sicurezza: configurazione NON valida. JwtAuthConverter è nullo.");
            throw new IllegalStateException("Configurazione sicurezza non valida: JwtAuthConverter nullo.");
        }

        if (publicEndpoints == null || publicEndpoints.length == 0) {
            log.warn("Sicurezza: PUBLIC_ENDPOINTS è nullo o vuoto. Tutti gli endpoint risulteranno protetti.");
        } else {
            boolean hasEmpty = Arrays.stream(publicEndpoints).anyMatch(e -> !StringUtils.hasText(e));
            if (hasEmpty) {
                log.warn("Sicurezza: PUBLIC_ENDPOINTS contiene valori vuoti o non validi. Verificare AppConstants.Security.PUBLIC_ENDPOINTS.");
            }
        }

        log.debug("Sicurezza: validazione configurazione completata con successo.");
    }

    /**
     * Catena filtri di sicurezza HTTP.
     *
     * <p>
     * Impostazioni principali:
     * </p>
     * <ul>
     *   <li>CSRF disabilitato (API stateless).</li>
     *   <li>CORS abilitato.</li>
     *   <li>Sessione stateless.</li>
     *   <li>Endpoint pubblici consentiti; resto autenticato.</li>
     *   <li>Resource server JWT con converter custom.</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Sicurezza: costruzione SecurityFilterChain.");

        http
                // API stateless: CSRF non necessario
                .csrf(AbstractHttpConfigurer::disable)

                // Abilita CORS: policy definita altrove (CorsConfig/CorsFilter o CorsConfigurationSource)
                .cors(Customizer.withDefaults())

                // Nessuna sessione server-side
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regole di autorizzazione
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AppConstants.Security.PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                // Resource server JWT con mapping custom delle authorities
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        SecurityFilterChain chain = http.build();

        log.debug("Sicurezza: SecurityFilterChain costruita correttamente.");
        log.debug("Sicurezza: riepilogo configurazione: publicEndpoints={}, stateless={}, jwtConverter={}.",
                AppConstants.Security.PUBLIC_ENDPOINTS == null ? "null" : Arrays.toString(AppConstants.Security.PUBLIC_ENDPOINTS),
                true,
                jwtAuthConverter.getClass().getName()
        );

        return chain;
    }
}
