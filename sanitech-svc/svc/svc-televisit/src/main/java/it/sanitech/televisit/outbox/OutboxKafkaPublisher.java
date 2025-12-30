package it.sanitech.televisit.outbox;

import it.sanitech.televisit.utilities.AppConstants;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
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
 *
 * <p>Per ogni evento non ancora pubblicato:
 * <ul>
 *   <li>tenta l'invio su Kafka con retry/backoff (Resilience4j {@link Retry});</li>
 *   <li>in caso di successo, marca l'evento come pubblicato (dirty checking JPA);</li>
 *   <li>in caso di fallimento, lascia l'evento non pubblicato per tentativi futuri.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    /**
     * Esegue periodicamente il flush degli eventi non pubblicati.
     * Frequenza controllata da {@code sanitech.outbox.publisher.delay-ms}.
     */
    @Scheduled(fixedDelayString = "${sanitech.outbox.publisher.delay-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> batch = outbox.lockBatch(100);

        for (OutboxEvent evt : batch) {
            try {
                RecordMetadata metadata = publishSingleWithRetry(evt);

                // Marca come pubblicato: il salvataggio avviene su DB al commit della transazione.
                evt.markPublished(Instant.ofEpochMilli(metadata.timestamp()));

                meterRegistry.counter(
                        AppConstants.Outbox.METRIC_EVENTS_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={}, type={}, event={}): {}",
                        evt.getId(), evt.getAggregateType(), evt.getEventType(), ex.toString());
            }
        }
    }

    /**
     * Pubblica un singolo evento su Kafka con retry/backoff.
     *
     * <p>La policy di retry è configurata in {@code resilience4j.retry.instances.outboxPublish}.</p>
     */
    @Retry(name = "outboxPublish")
    protected RecordMetadata publishSingleWithRetry(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();
        return kafkaTemplate.send(AppConstants.Outbox.TOPIC_TELEVISIT_EVENTS, key, evt.getPayload())
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }
}
