package it.sanitech.payments.outbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import it.sanitech.outbox.autoconfigure.OutboxProperties;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import it.sanitech.outbox.publisher.OutboxKafkaPublisher;
import it.sanitech.outbox.publisher.OutboxKafkaSender;
import it.sanitech.payments.utilities.AppConstants;
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

class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_shouldMarkPublishedAndIncrementMetric() {
        OutboxRepository repo = mock(OutboxRepository.class);
        OutboxKafkaSender sender = mock(OutboxKafkaSender.class);
        TransactionTemplate tx = mock(TransactionTemplate.class);
        SimpleMeterRegistry meter = new SimpleMeterRegistry();

        OutboxProperties props = new OutboxProperties();
        props.setEnabled(true);
        props.getPublisher().setEnabled(true);
        props.getPublisher().setBatchSize(100);
        props.getPublisher().setSendTimeoutMs(1000);
        props.getPublisher().setTopic("payments.events");

        OutboxEvent evt = OutboxEvent.newUnpublished(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT,
                "10",
                AppConstants.Outbox.EVT_CREATED,
                "{\"a\":1}"
        );
        evt.setId(1L);

        doAnswer(invocation -> {
            java.util.function.Consumer<?> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(tx).executeWithoutResult(any());

        when(repo.lockBatch(100)).thenReturn(List.of(evt));

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(tx, repo, sender, props, meter);
        publisher.publishBatch();

        verify(sender).sendSync(props.getPublisher().getTopic(), evt, props.getPublisher().getSendTimeoutMs());
        verify(repo).markPublished(eq(List.of(1L)), any());
        assertThat(evt.isPublished()).isFalse();

        double count = meter.find("sanitech.outbox.published").counter().count();
        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }
}
