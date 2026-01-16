package it.sanitech.scheduling.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test del publisher outbox → Kafka.
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marks_event_as_published_on_success() throws Exception {
        OutboxRepository repo = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);

        // Sender OK: possiamo restituire null metadata, il publisher gestisce il caso.
        when(sender.send(any())).thenReturn(null);

        ObjectMapper om = new ObjectMapper();
        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType("DOCTOR")
                .aggregateId("1")
                .eventType("DOCTOR_CREATED")
                .payload(om.readTree("{\"id\":1}"))
                .build();

        when(repo.lockBatch(100)).thenReturn(List.of(evt));

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(repo, sender, registry);

        publisher.publishBatch();

        assertThat(evt.isPublished()).isTrue();
        verify(repo, times(1)).save(evt);
        verify(sender, times(1)).send(evt);
    }
}
