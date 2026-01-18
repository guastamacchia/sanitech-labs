package it.sanitech.commons.autoconfigure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.sanitech.commons.autoconfigure.properties.OpenApiProperties;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Configurazione OpenAPI (Springdoc).
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnClass(GroupedOpenApi.class)
@EnableConfigurationProperties(OpenApiProperties.class)
@ConditionalOnProperty(prefix = AppConstants.ConfigKeys.OpenApi.PREFIX, name = "enabled", havingValue = "true")
public class OpenApiAutoConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    private final OpenApiProperties props;

    /**
     * Validazione della configurazione OpenAPI.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("OpenAPI: avvio validazione configurazione Springdoc.");

        String group = props.getGroup();
        List<String> packages = Optional.ofNullable(props.getPackagesToScan()).orElse(List.of());

        if (!StringUtils.hasText(group)) {
            log.error("OpenAPI: configurazione non valida. Il nome del gruppo è vuoto o nullo.");
            throw new IllegalStateException("Configurazione OpenAPI non valida: group vuoto.");
        }

        if (packages.isEmpty()) {
            log.error("OpenAPI: configurazione non valida. packagesToScan è vuoto o nullo.");
            throw new IllegalStateException("Configurazione OpenAPI non valida: packagesToScan vuoto.");
        }

        // Title e version sono opzionali: se non presenti, non blocchiamo la startup.
        if (!StringUtils.hasText(props.getTitle())) {
            log.warn("OpenAPI: title non valorizzato. Verrà usato l'eventuale valore predefinito di Springdoc.");
        }
        if (!StringUtils.hasText(props.getVersion())) {
            log.warn("OpenAPI: version non valorizzato. Verrà usato l'eventuale valore predefinito di Springdoc.");
        }

        log.debug("OpenAPI: validazione configurazione completata con successo.");
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceApi")
    public GroupedOpenApi serviceApi() {
        log.debug("OpenAPI: creazione GroupedOpenApi per il gruppo '{}'.", props.getGroup());

        String[] pkgs = Optional.ofNullable(props.getPackagesToScan()).orElse(List.of()).toArray(String[]::new);

        GroupedOpenApi api = GroupedOpenApi.builder()
            .group(props.getGroup())
            .packagesToScan(pkgs)
            .addOpenApiCustomizer(this::applyDefaults)
            .build();

        log.debug("OpenAPI: GroupedOpenApi creato correttamente.");
        return api;
    }

    /**
     * Applica le impostazioni di default al modello OpenAPI.
     */
    private void applyDefaults(OpenAPI openApi) {
        if (Objects.isNull(openApi)) {
            log.warn("OpenAPI: modello OpenAPI nullo ricevuto dal customizer. Nessuna modifica applicata.");
            return;
        }

        log.debug("OpenAPI: applicazione impostazioni di default al modello OpenAPI.");

        applyInfoDefaults(openApi);
        applyBearerSecurity(openApi);

        log.debug("OpenAPI: impostazioni di default applicate correttamente.");
    }

    private void applyInfoDefaults(OpenAPI openApi) {
        Info info = openApi.getInfo();
        if (Objects.isNull(info)) {
            info = new Info();
            openApi.setInfo(info);
            log.debug("OpenAPI: sezione Info assente. Creata nuova istanza.");
        }

        String title = props.getTitle();
        if (!StringUtils.hasText(info.getTitle()) && StringUtils.hasText(title)) {
            info.setTitle(title);
            log.debug("OpenAPI: impostato title='{}'.", title);
        }

        String version = props.getVersion();
        if (!StringUtils.hasText(info.getVersion()) && StringUtils.hasText(version)) {
            info.setVersion(version);
            log.debug("OpenAPI: impostata version='{}'.", version);
        }
    }

    private void applyBearerSecurity(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (Objects.isNull(components)) {
            components = new Components();
            openApi.setComponents(components);
            log.debug("OpenAPI: sezione Components assente. Creata nuova istanza.");
        }

        if (Objects.isNull(components.getSecuritySchemes())
        || !components.getSecuritySchemes().containsKey(BEARER_AUTH)) {
            components.addSecuritySchemes(BEARER_AUTH,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization"));

            log.debug("OpenAPI: aggiunto SecurityScheme '{}' (HTTP bearer JWT).", BEARER_AUTH);
        } else {
            log.debug("OpenAPI: SecurityScheme '{}' già presente.", BEARER_AUTH);
        }

        if (!hasBearerRequirement(openApi.getSecurity())) {
            openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
            log.debug("OpenAPI: aggiunto SecurityRequirement per '{}'.", BEARER_AUTH);
        } else {
            log.debug("OpenAPI: SecurityRequirement per '{}' già presente.", BEARER_AUTH);
        }
    }

    private static boolean hasBearerRequirement(List<SecurityRequirement> security) {
        if (Objects.isNull(security) || security.isEmpty()) {
            return false;
        }
        return security.stream()
            .filter(Objects::nonNull)
            .anyMatch(req -> req.containsKey(OpenApiAutoConfiguration.BEARER_AUTH));
    }
}
