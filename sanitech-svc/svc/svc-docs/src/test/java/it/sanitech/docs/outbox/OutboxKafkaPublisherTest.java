package it.sanitech.docs.outbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit test del publisher Kafka (senza broker reale).
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marksEventPublished_andIncrementsMetric() throws Exception {

        OutboxRepository repo = Mockito.mock(OutboxRepository.class);
        KafkaTemplate<String, String> kafka = Mockito.mock(KafkaTemplate.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType("DOCUMENT")
                .aggregateId("123")
                .eventType("DOCUMENT_UPLOADED")
                .payload("{\"k\":\"v\"}")
                .build();

        when(repo.lockBatch(100)).thenReturn(List.of(evt));

        RecordMetadata metadata = Mockito.mock(RecordMetadata.class);
        when(metadata.timestamp()).thenReturn(Instant.now().toEpochMilli());

        SendResult<String, String> sendResult = new SendResult<>(
                new org.apache.kafka.clients.producer.ProducerRecord<>("docs.events", "k", "{}"),
                metadata
        );
        when(kafka.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(repo, kafka, registry);
        publisher.publishBatch();

        assertThat(evt.isPublished()).isTrue();

        double count = registry.find("outbox.events.published").counter().count();
        assertThat(count).isEqualTo(1.0);
    }
}
