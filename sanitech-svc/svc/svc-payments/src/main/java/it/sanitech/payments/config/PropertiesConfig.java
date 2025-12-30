package it.sanitech.payments.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Abilita le {@code @ConfigurationProperties} del microservizio.
 */
@Configuration
@EnableConfigurationProperties({
        CorsProperties.class,
        OutboxPublisherProperties.class,
        PaymentWebhookProperties.class
})
public class PropertiesConfig { }
