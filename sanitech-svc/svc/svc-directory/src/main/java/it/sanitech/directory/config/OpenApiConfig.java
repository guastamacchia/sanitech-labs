package it.sanitech.directory.config;

import io.swagger.v3.oas.models.info.Info;
import it.sanitech.directory.utilities.AppConstants;
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
                .addOpenApiCustomizer(openApi -> openApi.setInfo(
                        new Info().title(AppConstants.OpenApi.TITLE)
                                  .version(AppConstants.OpenApi.VERSION))).build();
    }
}
