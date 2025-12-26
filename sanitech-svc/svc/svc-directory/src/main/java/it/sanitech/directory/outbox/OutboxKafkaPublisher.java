package it.sanitech.directory.outbox;

import it.sanitech.directory.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Publisher che legge gli eventi dalla tabella outbox e li invia a Kafka.
 *
 * <p>
 * Per ogni evento non ancora pubblicato:
 * <ul>
 *   <li>tenta la pubblicazione su Kafka con retry/backoff (Resilience4j);</li>
 *   <li>in caso di successo, marca l'evento come pubblicato;</li>
 *   <li>incrementa la metrica {@code outbox.events.published}.</li>
 * </ul>
 * In caso di fallimento, l'evento resta {@code published=false} e verrà ritentato nei cicli successivi.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final OutboxRepository outbox;
    private final OutboxKafkaSender sender;
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
                RecordMetadata metadata = sender.send(evt);

                // Salvataggio a DB: l'evento viene marcato "published=true" e persistito nella stessa transazione.
                evt.markPublished(timestampToInstant(metadata));
                outbox.save(evt);

                meterRegistry.counter(
                        AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()).increment();

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={}, aggregateType={}, eventType={}). Verrà ritentata.",
                        evt.getId(), evt.getAggregateType(), evt.getEventType(), ex);
            }
        }
    }

    private static Instant timestampToInstant(RecordMetadata metadata) {
        long ts = metadata != null ? metadata.timestamp() : -1L;
        return (ts > 0) ? Instant.ofEpochMilli(ts) : Instant.now();
    }
}
