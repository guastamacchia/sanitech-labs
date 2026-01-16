package it.sanitech.payments.outbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import it.sanitech.payments.config.OutboxPublisherProperties;
import it.sanitech.payments.utilities.AppConstants;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxKafkaPublisherTest {

    @Test
    void publishBatch_shouldMarkPublishedAndIncrementMetric() throws Exception {
        OutboxRepository repo = mock(OutboxRepository.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        SimpleMeterRegistry meter = new SimpleMeterRegistry();

        OutboxPublisherProperties props = new OutboxPublisherProperties();
        props.setBatchSize(100);
        props.setDelayMs(1000);

        OutboxEvent evt = OutboxEvent.newUnpublished(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT, "10", AppConstants.Outbox.EVT_CREATED, "{\"a\":1}"
        );

        when(repo.lockBatch(anyInt())).thenReturn(List.of(evt));

        RecordMetadata md = new RecordMetadata(
                new TopicPartition(AppConstants.Outbox.TOPIC_PAYMENTS_EVENTS, 0),
                0L, 0, System.currentTimeMillis(), 0, 0
        );
        SendResult<String, String> sendResult = new SendResult<>(
                new org.apache.kafka.clients.producer.ProducerRecord<>(AppConstants.Outbox.TOPIC_PAYMENTS_EVENTS, "k", evt.getPayload()),
                md
        );
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        OutboxKafkaPublisher publisher = new OutboxKafkaPublisher(repo, kafkaTemplate, meter, props);
        publisher.publishBatch();

        assertThat(evt.isPublished()).isTrue();
        assertThat(evt.getPublishedAt()).isNotNull();

        // verifica che sia stata effettuata una save dopo markPublished
        verify(repo, atLeastOnce()).save(any(OutboxEvent.class));

        // metrica incrementata
        double count = meter.get(AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED).counter().count();
        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }
}
