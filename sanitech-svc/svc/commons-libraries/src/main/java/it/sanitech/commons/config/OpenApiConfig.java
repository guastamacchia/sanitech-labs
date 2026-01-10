package it.sanitech.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.sanitech.commons.utilities.AppConstants;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI (Springdoc) del microservizio Directory.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi directoryApi() {
        return GroupedOpenApi.builder()
                .group(AppConstants.OpenApi.GROUP_DIRECTORY)
                .packagesToScan(AppConstants.OpenApi.PACKAGES_TO_SCAN)
                .addOpenApiCustomizer(this::applyDefaults)
                .build();
    }

    private void applyDefaults(OpenAPI openApi) {
        openApi.setInfo(new Info()
                .title(AppConstants.OpenApi.TITLE)
                .version(AppConstants.OpenApi.VERSION));

        Components components = openApi.getComponents();
        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        components.addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));

        openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
