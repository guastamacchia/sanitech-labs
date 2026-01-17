package it.sanitech.audit.outbox;

import it.sanitech.audit.utilities.AppConstants;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import it.sanitech.outbox.publisher.OutboxKafkaPublisher;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test di integrazione del publisher Kafka con outbox:
 * Kafka è mockato, DB è reale (Postgres Testcontainers).
 */
@Testcontainers
@SpringBootTest(properties = {
        "sanitech.outbox.publisher.enabled=true"
})
class OutboxKafkaPublisherTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sanitech_audit_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    OutboxKafkaPublisher publisher;

    @MockBean
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void publishBatch_shouldMarkEventAsPublishedOnSuccess() throws Exception {
        OutboxEvent e = OutboxEvent.builder()
                .aggregateType("AUDIT_EVENT")
                .aggregateId("1")
                .eventType("AUDIT_RECORDED")
                .payload(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("k", "v"))
                .build();

        OutboxEvent saved = outboxRepository.save(e);

        // Kafka send() mock: successo immediato
        String topic = AppConstants.Outbox.TOPIC_AUDIT_EVENTS;
        RecordMetadata meta = new RecordMetadata(new TopicPartition(topic, 0), 0, 0, System.currentTimeMillis(), 0L, 0, 0);
        ProducerRecord<String, String> pr = new ProducerRecord<>(topic, "AUDIT_EVENT:1", "{\"k\":\"v\"}");
        SendResult<String, String> sr = new SendResult<>(pr, meta);

        when(kafkaTemplate.send(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(CompletableFuture.completedFuture(sr));

        publisher.publishBatch();

        OutboxEvent reloaded = outboxRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.isPublished()).isTrue();
        assertThat(reloaded.getPublishedAt()).isNotNull();
    }
}
