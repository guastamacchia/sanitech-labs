package it.sanitech.commons.autoconfigure;

import it.sanitech.commons.autoconfigure.properties.SecurityProperties;
import it.sanitech.commons.security.JwtAuthConverter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import java.util.List;
import java.util.Objects;

/**
 * Configurazione di sicurezza del microservizio (Spring Security 6).
 *
 * <p>
 * Principi adottati:
 * </p>
 * <ul>
 *   <li>Il servizio è un Resource Server JWT (OIDC/Keycloak).</li>
 *   <li>Sessione stateless: ogni richiesta deve includere il token.</li>
 *   <li>Gli endpoint tecnici configurati nelle property {@code sanitech.security.public-endpoints} sono pubblici.</li>
 *   <li>Tutto il resto richiede autenticazione; autorizzazioni fine-grained via {@code @PreAuthorize}.</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnBean(JwtAuthConverter.class)
@ConditionalOnProperty(prefix = SecurityProperties.PREFIX, name = "enabled", havingValue = "true")
public class SecurityAutoConfiguration {

    private final JwtAuthConverter jwtAuthConverter;
    private final SecurityProperties props;

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

        List<String> publicEndpoints = props.getPublicEndpoints();

        log.debug("Sicurezza: policy sessione prevista: STATELESS.");
        log.debug("Sicurezza: CORS abilitato (policy definita nella configurazione CORS).");
        log.debug("Sicurezza: endpoint pubblici configurati: {}.",
                Objects.isNull(publicEndpoints) ? "null" : Arrays.toString(publicEndpoints.toArray(String[]::new)));

        if (Objects.isNull(jwtAuthConverter)) {
            log.error("Sicurezza: configurazione NON valida. JwtAuthConverter è nullo.");
            throw new IllegalStateException("Configurazione sicurezza non valida: JwtAuthConverter nullo.");
        }

        if (Objects.isNull(publicEndpoints) || publicEndpoints.isEmpty()) {
            log.warn("Sicurezza: publicEndpoints è nullo o vuoto. Tutti gli endpoint risulteranno protetti.");
        } else {
            boolean hasEmpty = publicEndpoints.stream().anyMatch(e -> !StringUtils.hasText(e));
            if (hasEmpty) {
                log.warn("Sicurezza: publicEndpoints contiene valori vuoti o non validi. Verificare sanitech.security.public-endpoints.");
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

        List<String> publicEndpoints = props.getPublicEndpoints();

        http
                // API stateless: CSRF non necessario
                .csrf(AbstractHttpConfigurer::disable)

                // Abilita CORS: policy definita altrove (CorsAutoConfiguration)
                .cors(Customizer.withDefaults())

                // Nessuna sessione server-side
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Regole di autorizzazione
                .authorizeHttpRequests(auth -> {
                    if (Objects.nonNull(publicEndpoints) && !publicEndpoints.isEmpty()) {
                        auth.requestMatchers(publicEndpoints.toArray(String[]::new)).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })

                // Resource server JWT con mapping custom delle authorities
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                );

        SecurityFilterChain chain = http.build();

        log.debug("Sicurezza: SecurityFilterChain costruita correttamente.");
        log.debug("Sicurezza: riepilogo configurazione: publicEndpoints={}, stateless={}, jwtConverter={}.",
                Objects.isNull(publicEndpoints) ? "null" : Arrays.toString(publicEndpoints.toArray(String[]::new)),
                true,
                jwtAuthConverter.getClass().getName()
        );

        return chain;
    }
}
