package it.sanitech.outbox.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import it.sanitech.outbox.boot.SanitechOutboxJpaConfiguration;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import it.sanitech.outbox.publisher.DefaultOutboxKafkaSender;
import it.sanitech.outbox.publisher.OutboxKafkaPublisher;
import it.sanitech.outbox.publisher.OutboxKafkaSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Auto-configurazione del publisher Kafka (job schedulato).
 *
 * <p>
 * Viene eseguita dopo {@link SanitechOutboxJpaConfiguration} e {@link KafkaAutoConfiguration}
 * per garantire che tutti i bean necessari siano già disponibili.
 * </p>
 */
@Slf4j
@AutoConfiguration(after = {
        SanitechOutboxJpaConfiguration.class,
        KafkaAutoConfiguration.class
})
@EnableScheduling
@ConditionalOnProperty(prefix = "sanitech.outbox.publisher", name = "enabled", havingValue = "true")
@ConditionalOnClass({KafkaTemplate.class, OutboxRepository.class, OutboxEvent.class, TransactionTemplate.class})
@ConditionalOnBean(PlatformTransactionManager.class)
public class OutboxPublisherAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionTemplate outboxTransactionTemplate(PlatformTransactionManager txManager) {
        log.debug("Outbox: creazione TransactionTemplate dedicato.");
        return new TransactionTemplate(txManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxKafkaSender outboxKafkaSender(KafkaTemplate<String, String> kafkaTemplate,
                                                ObjectMapper objectMapper) {
        log.debug("Outbox: creazione sender Kafka di default.");
        return new DefaultOutboxKafkaSender(kafkaTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxKafkaPublisher outboxKafkaPublisher(TransactionTemplate tx,
                                                     OutboxRepository outboxRepository,
                                                     OutboxKafkaSender sender,
                                                     OutboxProperties props,
                                                     MeterRegistry meterRegistry) {
        log.debug("Outbox: creazione publisher Kafka schedulato.");
        return new OutboxKafkaPublisher(tx, outboxRepository, sender, props, meterRegistry);
    }
}
