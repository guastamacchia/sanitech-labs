package it.sanitech.prescribing.config;

import io.swagger.v3.oas.models.info.Info;
import it.sanitech.prescribing.utilities.AppConstants;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI (Swagger) del microservizio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi prescribingApi() {
        return GroupedOpenApi.builder()
                .group(AppConstants.OpenApi.GROUP)
                .packagesToScan(AppConstants.OpenApi.PACKAGES_TO_SCAN)
                .addOpenApiCustomizer(openApi -> openApi.setInfo(
                        new Info()
                                .title(AppConstants.OpenApi.TITLE)
                                .version(AppConstants.OpenApi.VERSION)
                ))
                .build();
    }
}
