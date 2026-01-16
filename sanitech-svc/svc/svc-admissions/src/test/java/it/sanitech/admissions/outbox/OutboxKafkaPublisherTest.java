package it.sanitech.admissions.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test unitario del publisher outbox verso Kafka (KafkaTemplate non necessario: usiamo {@link OutboxKafkaSender} mock).
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marksEventAsPublished() {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);

        OutboxEvent evt = OutboxEvent.builder()
                .id(1L)
                .aggregateType("ADMISSION")
                .aggregateId("1")
                .eventType("ADMISSION_CREATED")
                .payload(new ObjectMapper().createObjectNode().put("a", 1))
                .occurredAt(Instant.now())
                .published(false)
                .build();

        when(outboxRepository.lockBatch(100)).thenReturn(List.of(evt));
        when(sender.sendWithRetry(anyString(), anyString(), anyString())).thenReturn(null);

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(outboxRepository, sender, new SimpleMeterRegistry());
        publisher.publishBatch();

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getPublishedAt()).isNotNull();
    }
}
