package it.sanitech.notifications.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario del publisher outbox: marca published e incrementa metriche.
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marks_event_published_on_success() {
        OutboxRepository repo = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(repo, sender, meterRegistry);

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType("NOTIFICATION")
                .aggregateId("1")
                .eventType("NOTIFICATION_CREATED")
                .payload(new ObjectMapper().createObjectNode().put("id", 1))
                .published(false)
                .build();

        when(repo.lockBatch()).thenReturn(List.of(evt));

        RecordMetadata md = mock(RecordMetadata.class);
        when(md.timestamp()).thenReturn(Instant.now().toEpochMilli());

        when(sender.sendWithRetry(anyString(), anyString(), anyString())).thenReturn(md);

        publisher.publishBatch();

        assertThat(evt.isPublished()).isTrue();
        assertThat(evt.getPublishedAt()).isNotNull();

        assertThat(meterRegistry.find("outbox.events.published.count").counter()).isNotNull();
        assertThat(meterRegistry.find("outbox.events.published.count").counter().count()).isEqualTo(1.0);

        verify(sender, times(1)).sendWithRetry(anyString(), anyString(), anyString());
    }
}
