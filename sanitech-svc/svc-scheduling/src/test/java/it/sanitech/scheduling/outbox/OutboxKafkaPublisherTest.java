package it.sanitech.scheduling.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import it.sanitech.outbox.autoconfigure.OutboxProperties;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import it.sanitech.outbox.publisher.OutboxKafkaPublisher;
import it.sanitech.outbox.publisher.OutboxKafkaSender;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test del publisher outbox → Kafka.
 */
class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_marks_event_as_published_on_success() throws Exception {
        OutboxRepository repo = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);
        TransactionTemplate tx = mock(TransactionTemplate.class);

        OutboxProperties props = new OutboxProperties();
        props.setEnabled(true);
        props.getPublisher().setEnabled(true);
        props.getPublisher().setBatchSize(100);
        props.getPublisher().setSendTimeoutMs(1000);
        props.getPublisher().setTopic("scheduling.events");

        doAnswer(invocation -> {
            java.util.function.Consumer<?> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(tx).executeWithoutResult(any());

        ObjectMapper om = new ObjectMapper();
        OutboxEvent evt = OutboxEvent.builder()
                .id(1L)
                .aggregateType("DOCTOR")
                .aggregateId("1")
                .eventType("DOCTOR_CREATED")
                .payload(om.readTree("{\"id\":1}"))
                .build();

        when(repo.lockBatch(100)).thenReturn(List.of(evt));

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(tx, repo, sender, props, registry);

        publisher.publishBatch();

        verify(sender).sendSync(props.getPublisher().getTopic(), evt, props.getPublisher().getSendTimeoutMs());
        verify(repo).markPublished(eq(List.of(1L)), any());
        assertThat(evt.isPublished()).isFalse();
    }
}
