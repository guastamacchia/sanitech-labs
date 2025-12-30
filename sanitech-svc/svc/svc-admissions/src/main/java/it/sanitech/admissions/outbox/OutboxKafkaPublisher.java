package it.sanitech.admissions.outbox;

import it.sanitech.admissions.utilities.AppConstants;
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
 * Flusso:
 * <ol>
 *   <li>Seleziona un batch di eventi non pubblicati con lock ({@code FOR UPDATE SKIP LOCKED});</li>
 *   <li>Invia ogni evento su Kafka con retry/backoff (Resilience4j);</li>
 *   <li>In caso di successo, marca {@code published=true} e persiste l'aggiornamento;</li>
 *   <li>In caso di fallimento, lascia l'evento non pubblicato per i cicli successivi.</li>
 * </ol>
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
        if (batch.isEmpty()) {
            return;
        }

        for (OutboxEvent evt : batch) {
            try {
                String key = evt.getAggregateType() + ":" + evt.getAggregateId();
                String payloadJson = evt.getPayload().toString();

                RecordMetadata metadata = sender.sendWithRetry(
                        AppConstants.Outbox.TOPIC_ADMISSIONS_EVENTS,
                        key,
                        payloadJson
                );

                // Persistiamo published=true; in JPA il flush avviene a commit, ma facciamo save esplicito per chiarezza.
                Instant publishedAt = (metadata != null && metadata.timestamp() > 0)
                        ? Instant.ofEpochMilli(metadata.timestamp())
                        : Instant.now();
                evt.markPublished(publishedAt);
                outbox.save(evt);

                meterRegistry.counter(
                        AppConstants.Outbox.METRIC_OUTBOX_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={} aggregate={}:{} eventType={}): {}",
                        evt.getId(), evt.getAggregateType(), evt.getAggregateId(), evt.getEventType(), ex.getMessage());
            }
        }
    }
}
