package it.sanitech.docs.outbox;

import it.sanitech.docs.utilities.AppConstants;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
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
 *   <li>tenta la pubblicazione su Kafka con retry/backoff (Resilience4j {@link Retry});</li>
 *   <li>in caso di successo, marca l’evento come pubblicato;</li>
 *   <li>incrementa la metrica {@code outbox.events.published} con tag di tipo ed evento;</li>
 *   <li>in caso di fallimento, lascia l’evento non pubblicato per i cicli futuri.</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    /**
     * Esegue periodicamente il flush degli eventi non pubblicati.
     * La frequenza è controllata da {@code sanitech.outbox.publisher.delay-ms}.
     */
    @Scheduled(fixedDelayString = "${sanitech.outbox.publisher.delay-ms:1000}")
    @Transactional
    public void publishBatch() {
        List<OutboxEvent> batch = outbox.lockBatch(100);

        for (OutboxEvent evt : batch) {
            try {
                RecordMetadata metadata = publishSingleWithRetry(evt);

                // Poiché evt è una entity JPA gestita nella transazione corrente,
                // l'update (published=true) viene salvato automaticamente al commit.
                evt.markPublished(Instant.ofEpochMilli(metadata.timestamp()));

                meterRegistry.counter(
                        AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                // Non marchiamo l'evento come pubblicato: verrà ripreso nei cicli successivi.
            }
        }
    }

    /**
     * Pubblica un singolo evento su Kafka.
     *
     * <p>
     * Annotata con {@link Retry} per applicare retry + backoff configurati
     * nella sezione {@code resilience4j.retry.instances.outboxPublish}.
     * </p>
     */
    @Retry(name = "outboxPublish")
    protected RecordMetadata publishSingleWithRetry(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();

        return kafkaTemplate.send(AppConstants.Outbox.TOPIC_DOCS_EVENTS, key, evt.getPayload())
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }
}
