package it.sanitech.payments.outbox;

import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import it.sanitech.payments.config.OutboxPublisherProperties;
import it.sanitech.payments.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Publisher che legge gli eventi dalla tabella outbox e li invia a Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private final OutboxPublisherProperties props;

    /**
     * Esegue periodicamente il flush degli eventi non pubblicati.
     * La frequenza è controllata da {@code sanitech.outbox.publisher.delay-ms}.
     */
    @Scheduled(fixedDelayString = "${sanitech.outbox.publisher.delay-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> batch = outbox.lockBatch(props.getBatchSize());

        for (OutboxEvent evt : batch) {
            try {
                RecordMetadata metadata = publishSingleWithRetry(evt);

                // Persistenza dello stato "published=true": avviene aggiornando l'entità e salvandola via JPA.
                evt.markPublished(Instant.ofEpochMilli(metadata.timestamp()));
                outbox.save(evt);

                meterRegistry.counter(
                        AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={}): verrà ritentata. Motivo={}", evt.getId(), ex.getMessage());
            }
        }
    }

    /**
     * Pubblica un singolo evento su Kafka con retry/backoff (Resilience4j).
     */
    @Retry(name = "outboxPublish")
    protected RecordMetadata publishSingleWithRetry(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();
        return kafkaTemplate.send(AppConstants.Outbox.TOPIC_PAYMENTS_EVENTS, key, evt.getPayload())
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }
}
