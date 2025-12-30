package it.sanitech.televisit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Abilita il binding delle proprietà custom (prefisso {@code sanitech.*}).
 */
@Configuration
@EnableConfigurationProperties({
        CorsProperties.class,
        OpenApiProperties.class,
        LiveKitProperties.class
})
public class PropertiesConfig {
}
