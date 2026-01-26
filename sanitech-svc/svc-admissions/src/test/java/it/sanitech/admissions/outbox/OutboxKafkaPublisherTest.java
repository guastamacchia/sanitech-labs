package it.sanitech.admissions.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import it.sanitech.outbox.autoconfigure.OutboxProperties;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import it.sanitech.outbox.publisher.OutboxKafkaPublisher;
import it.sanitech.outbox.publisher.OutboxKafkaSender;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario del publisher outbox verso Kafka (KafkaTemplate non necessario: usiamo {@link OutboxKafkaSender} mock).
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marksEventAsPublished() {
        OutboxRepository outboxRepository = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);
        TransactionTemplate tx = mock(TransactionTemplate.class);

        OutboxProperties props = new OutboxProperties();
        props.setEnabled(true);
        props.getPublisher().setEnabled(true);
        props.getPublisher().setBatchSize(100);
        props.getPublisher().setSendTimeoutMs(1000);
        props.getPublisher().setTopic("admissions.events");

        OutboxEvent evt = OutboxEvent.builder()
                .id(1L)
                .aggregateType("ADMISSION")
                .aggregateId("1")
                .eventType("ADMISSION_CREATED")
                .payload(new ObjectMapper().createObjectNode().put("a", 1))
                .occurredAt(Instant.now())
                .published(false)
                .build();

        doAnswer(invocation -> {
            java.util.function.Consumer<?> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(tx).executeWithoutResult(any());

        when(outboxRepository.lockBatch(100)).thenReturn(List.of(evt));

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(tx, outboxRepository, sender, props, new SimpleMeterRegistry());
        publisher.publishBatch();

        verify(sender).sendSync(props.getPublisher().getTopic(), evt, props.getPublisher().getSendTimeoutMs());
        verify(outboxRepository).markPublished(eq(List.of(1L)), any());
        assertThat(evt.isPublished()).isFalse();
    }
}
