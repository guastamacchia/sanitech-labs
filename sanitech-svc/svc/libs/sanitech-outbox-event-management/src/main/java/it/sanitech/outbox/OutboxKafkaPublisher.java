package it.sanitech.outbox;

import it.sanitech.commons.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Publisher che legge gli eventi dalla tabella outbox e li invia a Kafka.
 *
 * <p>
 * Policy: at-least-once delivery. In caso di crash tra send Kafka e update database,
 * l'evento può essere ripubblicato: i consumer devono essere idempotenti.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

    private final OutboxRepository outbox;
    private final OutboxKafkaSender sender;
    private final MeterRegistry meterRegistry;
    private final TransactionTemplate tx;

    /**
     * Esegue periodicamente il flush degli eventi non pubblicati.
     * La frequenza è controllata da {@code sanitech.outbox.publisher.delay-ms}.
     */
    @Scheduled(fixedDelayString = "${sanitech.outbox.publisher.delay-ms:1000}")
    public void publishBatch() {
        // Lock e fetch batch (SELECT ... FOR UPDATE SKIP LOCKED nel repository).
        List<OutboxEvent> batch = outbox.lockBatch(AppConstants.ConfigDefaultValue.Outbox.DEFAULT_BATCH_SIZE);

        for (OutboxEvent evt : batch) {
            try {
                // Invia a Kafka fuori transazione database (evita transazioni lunghe e lock prolungati).
                RecordMetadata metadata = sender.send(evt);

                // Aggiorna stato su DB in una transazione breve per singolo evento.
                tx.executeWithoutResult(status -> {
                    evt.markPublished(timestampToInstant(metadata));
                    outbox.save(evt);

                    meterRegistry.counter(
                            AppConstants.Outbox.OUTBOX_EVENTS_PUBLISHED,
                            AppConstants.Outbox.TAG_AGGREGATE_TYPE, safeTag(evt.getAggregateType()),
                            AppConstants.Outbox.TAG_EVENT_TYPE, safeTag(evt.getEventType())
                    ).increment();
                });

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={}, aggregateType={}, eventType={}). Verrà ritentata. Cause={}",
                        evt.getId(), evt.getAggregateType(), evt.getEventType(), ex.getMessage(), ex);
            }
        }
    }

    private static Instant timestampToInstant(RecordMetadata metadata) {
        return Optional.ofNullable(metadata)
                .map(RecordMetadata::timestamp)
                .filter(ts -> ts > 0)
                .map(Instant::ofEpochMilli)
                .orElseGet(Instant::now);
    }

    private static String safeTag(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .orElse(AppConstants.ErrorMessage.FALLBACK_VALUE);
    }
}
