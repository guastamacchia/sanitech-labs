package it.sanitech.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Configurazione OpenAPI (Springdoc).
 *
 * <p>
 * Responsabilità della classe:
 * </p>
 * <ul>
 *   <li>definire il gruppo OpenAPI e i package da scansionare</li>
 *   <li>validare e diagnosticare la configurazione a startup</li>
 *   <li>applicare impostazioni di default (Info + sicurezza JWT bearer)</li>
 * </ul>
 *
 * <p>
 * Le impostazioni vengono applicate in modo idempotente per evitare duplicazioni
 * in presenza di più OpenApiCustomizer.
 * </p>
 */
@Slf4j
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Validazione della configurazione OpenAPI.
     */
    @PostConstruct
    public void validateConfiguration() {
        log.debug("OpenAPI: avvio validazione configurazione Springdoc.");

        String group = AppConstants.OpenApi.GROUP_DIRECTORY;
        String packages = AppConstants.OpenApi.PACKAGES_TO_SCAN;
        String title = AppConstants.OpenApi.TITLE;
        String version = AppConstants.OpenApi.VERSION;

        log.debug("OpenAPI: group='{}'.", group);
        log.debug("OpenAPI: packagesToScan='{}'.", packages);
        log.debug("OpenAPI: title='{}'.", title);
        log.debug("OpenAPI: version='{}'.", version);

        if (!StringUtils.hasText(group)) {
            log.error("OpenAPI: configurazione NON valida. Il nome del gruppo è vuoto o nullo.");
            throw new IllegalStateException("Configurazione OpenAPI non valida: group vuoto.");
        }

        if (!StringUtils.hasText(packages)) {
            log.error("OpenAPI: configurazione NON valida. packagesToScan è vuoto o nullo.");
            throw new IllegalStateException("Configurazione OpenAPI non valida: packagesToScan vuoto.");
        }

        if (!StringUtils.hasText(title)) {
            log.warn("OpenAPI: title non valorizzato.");
        }

        if (!StringUtils.hasText(version)) {
            log.warn("OpenAPI: version non valorizzata.");
        }

        log.debug("OpenAPI: validazione configurazione completata con successo.");
    }

    @Bean
    public GroupedOpenApi directoryApi() {
        log.debug("OpenAPI: creazione GroupedOpenApi per il gruppo '{}'.",
                AppConstants.OpenApi.GROUP_DIRECTORY);

        GroupedOpenApi api = GroupedOpenApi.builder()
                .group(AppConstants.OpenApi.GROUP_DIRECTORY)
                .packagesToScan(AppConstants.OpenApi.PACKAGES_TO_SCAN)
                .addOpenApiCustomizer(this::applyDefaults)
                .build();

        log.debug("OpenAPI: GroupedOpenApi creato correttamente.");
        return api;
    }

    /**
     * Applica le impostazioni di default al modello OpenAPI.
     *
     * <p>
     * Le impostazioni vengono applicate solo se assenti, così da non sovrascrivere
     * eventuali personalizzazioni definite altrove.
     * </p>
     */
    private void applyDefaults(OpenAPI openApi) {
        if (openApi == null) {
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
        if (info == null) {
            info = new Info();
            openApi.setInfo(info);
            log.debug("OpenAPI: sezione Info assente. Creata nuova istanza.");
        }

        if (!StringUtils.hasText(info.getTitle()) && StringUtils.hasText(AppConstants.OpenApi.TITLE)) {
            info.setTitle(AppConstants.OpenApi.TITLE);
            log.debug("OpenAPI: impostato title='{}'.", AppConstants.OpenApi.TITLE);
        }

        if (!StringUtils.hasText(info.getVersion()) && StringUtils.hasText(AppConstants.OpenApi.VERSION)) {
            info.setVersion(AppConstants.OpenApi.VERSION);
            log.debug("OpenAPI: impostata version='{}'.", AppConstants.OpenApi.VERSION);
        }
    }

    private void applyBearerSecurity(OpenAPI openApi) {
        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
            log.debug("OpenAPI: sezione Components assente. Creata nuova istanza.");
        }

        if (components.getSecuritySchemes() == null ||
                !components.getSecuritySchemes().containsKey(BEARER_AUTH)) {

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
        if (security == null || security.isEmpty()) {
            return false;
        }
        return security.stream()
                .filter(Objects::nonNull)
                .anyMatch(req -> req.containsKey(OpenApiConfig.BEARER_AUTH));
    }
}
