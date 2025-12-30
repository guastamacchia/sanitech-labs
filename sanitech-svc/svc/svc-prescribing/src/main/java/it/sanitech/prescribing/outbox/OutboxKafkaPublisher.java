package it.sanitech.prescribing.outbox;

import it.sanitech.prescribing.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Publisher che legge gli eventi dalla tabella outbox e li invia a Kafka.
 *
 * <p>Per ogni evento non ancora pubblicato:</p>
 * <ul>
 *   <li>tenta la pubblicazione su Kafka con retry/backoff (Resilience4j);</li>
 *   <li>in caso di successo marca l'evento come pubblicato;</li>
 *   <li>incrementa la metrica {@code outbox.events.published.count} con tag aggregateType/eventType;</li>
 *   <li>in caso di fallimento lascia l'evento non pubblicato per i tentativi futuri.</li>
 * </ul>
 */
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
                RecordMetadata metadata = sender.publish(evt);

                // Marca come pubblicato: essendo l'entità "managed", JPA persiste il flag al commit.
                evt.markPublished(Instant.ofEpochMilli(metadata.timestamp()));

                meterRegistry.counter(
                        AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ignored) {
                // Intenzionale: l'evento rimane non pubblicato e verrà ripreso nel ciclo successivo.
            }
        }
    }
}
