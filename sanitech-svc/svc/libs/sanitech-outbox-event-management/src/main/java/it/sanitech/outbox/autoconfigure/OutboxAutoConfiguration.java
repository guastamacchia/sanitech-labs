package it.sanitech.outbox.autoconfigure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnClass(KafkaTemplate.class)
public class OutboxAutoConfiguration {
    // Qui non serve altro: abilitiamo solo il binding delle properties.
}
