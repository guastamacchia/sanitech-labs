package it.sanitech.televisit.config;

import io.swagger.v3.oas.models.info.Info;
import it.sanitech.televisit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI/Swagger (springdoc).
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final OpenApiProperties props;

    @Bean
    public GroupedOpenApi televisitApi() {
        return GroupedOpenApi.builder()
                .group(AppConstants.OpenApi.GROUP)
                .packagesToScan(AppConstants.OpenApi.PACKAGES_TO_SCAN)
                .addOpenApiCustomizer(openApi -> openApi.setInfo(
                        new Info()
                                .title(props.getTitle())
                                .version(props.getVersion())
                ))
                .build();
    }
}
