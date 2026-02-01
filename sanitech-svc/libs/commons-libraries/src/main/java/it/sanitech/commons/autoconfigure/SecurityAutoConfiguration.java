package it.sanitech.commons.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.commons.autoconfigure.properties.SecurityProperties;
import it.sanitech.commons.exception.ProblemDetails;
import it.sanitech.commons.security.JwtAuthConverter;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configurazione di sicurezza del microservizio (Spring Security 6).
 *
 * <p>
 * Principi adottati:
 * </p>
 * <ul>
 *   <li>Il servizio di AuthN/AuthZ è un Resource Server JWT (OIDC/Keycloak).</li>
 *   <li>Sessione stateless: ogni richiesta deve includere il token.</li>
 *   <li>Gli endpoint tecnici configurati nelle property {@code sanitech.security.public-endpoints} sono pubblici.</li>
 *   <li>Tutto il resto richiede autenticazione; autorizzazioni fine-grained via {@code @PreAuthorize}.</li>
 * </ul>
 */
@Slf4j
@AutoConfiguration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnBean(JwtAuthConverter.class)
@ConditionalOnProperty(prefix = AppConstants.ConfigKeys.Security.PREFIX, name = "enabled", havingValue = "true")
public class SecurityAutoConfiguration {

    private final JwtAuthConverter jwtAuthConverter;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Validazione e diagnostica della configurazione a startup.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("Sicurezza: avvio validazione configurazione Spring Security.");

        // In condizioni normali non è null (ConditionalOnBean + injection), ma rendiamo il check esplicito.
        if (Objects.isNull(jwtAuthConverter)) {
            log.error("Sicurezza: configurazione non valida. JwtAuthConverter è nullo.");
            throw new IllegalStateException("Configurazione sicurezza non valida: JwtAuthConverter nullo.");
        }

        List<String> publicEndpoints = Optional.ofNullable(properties.getPublicEndpoints()).orElse(List.of());

        log.debug("Sicurezza: policy sessione prevista: STATELESS.");
        log.debug("Sicurezza: CORS abilitato (policy definita dalla configurazione CORS).");

        if (publicEndpoints.isEmpty()) {
            log.warn("Sicurezza: publicEndpoints è vuoto. Tutti gli endpoint risulteranno protetti.");
        } else {
            // SecurityProperties normalizza già (trim, remove empty). Se qui troviamo comunque vuoti, è un segnale di uso non standard.
            boolean hasBlank = publicEndpoints.stream().anyMatch(s -> Objects.isNull(s) || s.isBlank());
            if (hasBlank) {
                log.warn("Sicurezza: publicEndpoints contiene valori vuoti/non validi dopo normalizzazione. Verificare sanitech.security.public-endpoints.");
            }
            log.debug("Sicurezza: numero endpoint pubblici configurati: {}", publicEndpoints.size());
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
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Sicurezza: costruzione SecurityFilterChain.");

        String[] publicMatchers = Optional.ofNullable(properties.getPublicEndpoints()).orElse(List.of()).toArray(String[]::new);

        http
            // API stateless: CSRF non necessario
            .csrf(AbstractHttpConfigurer::disable)

            // Abilita CORS: policy definita altrove (CorsAutoConfiguration)
            .cors(Customizer.withDefaults())

            // Nessuna sessione server-side
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Regole di autorizzazione
            .authorizeHttpRequests(auth -> {
                if (publicMatchers.length > 0) {
                    auth.requestMatchers(publicMatchers).permitAll();
                }
                auth.anyRequest().authenticated();
            })

            // Resource server JWT con mapping custom delle authorities
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            )

            // Gestione esplicita delle eccezioni di sicurezza
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            );

        SecurityFilterChain chain = http.build();

        log.debug("Sicurezza: SecurityFilterChain costruita correttamente.");
        log.debug("Sicurezza: riepilogo runtime: stateless={}, publicMatchersCount={}, jwtConverter={}.",
                true,
                publicMatchers.length,
                jwtAuthConverter.getClass().getName()
        );

        return chain;
    }

    /**
     * Handler per le eccezioni di accesso negato.
     *
     * <p>
     * Restituisce una risposta in formato RFC 7807 (ProblemDetails) con status 403 Forbidden.
     * </p>
     */
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ProblemDetails problem = ProblemDetails.builder()
                    .type(AppConstants.Problem.TYPE_FORBIDDEN)
                    .title(AppConstants.ErrorMessage.ERR_FORBIDDEN)
                    .status(HttpStatus.FORBIDDEN.value())
                    .detail(accessDeniedException.getMessage())
                    .instance(request.getRequestURI())
                    .build();

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }
}
