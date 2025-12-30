package it.sanitech.docs.config;

import io.swagger.v3.oas.models.info.Info;
import it.sanitech.docs.utilities.AppConstants;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI/Swagger per il microservizio.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi docsApi() {
        return GroupedOpenApi.builder()
                .group(AppConstants.Service.OPENAPI_GROUP)
                .packagesToScan("it.sanitech.docs.web")
                .addOpenApiCustomizer(openApi -> openApi.setInfo(
                        new Info()
                                .title(AppConstants.Service.OPENAPI_TITLE)
                                .version(AppConstants.Service.OPENAPI_VERSION)
                ))
                .build();
    }
}
