package it.sanitech.payments.config;

import io.swagger.v3.oas.models.info.Info;
import it.sanitech.payments.utilities.AppConstants;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione OpenAPI (springdoc).
 *
 * <p>
 * Definisce un gruppo unico (payments) e limita la scansione ai controller REST del servizio.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi paymentsApi() {
        return GroupedOpenApi.builder()
                .group(AppConstants.OpenApi.GROUP)
                .packagesToScan(AppConstants.OpenApi.PACKAGES_TO_SCAN)
                .addOpenApiCustomizer(openApi ->
                        openApi.setInfo(new Info()
                                .title(AppConstants.OpenApi.TITLE)
                                .version(AppConstants.OpenApi.VERSION)))
                .build();
    }
}
