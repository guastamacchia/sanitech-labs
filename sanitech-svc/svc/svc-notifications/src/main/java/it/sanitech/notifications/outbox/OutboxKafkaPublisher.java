package it.sanitech.notifications.outbox;

import it.sanitech.notifications.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Publisher schedulato che legge gli eventi dalla tabella outbox e li invia a Kafka.
 *
 * <p>
 * In caso di successo marca l'evento come pubblicato; in caso di errore lo lascia non pubblicato
 * (verrà ripreso nei cicli successivi).
 * </p>
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
        List<OutboxEvent> batch = outbox.lockBatch();

        for (OutboxEvent evt : batch) {
            try {
                String key = evt.getAggregateType() + ":" + evt.getAggregateId();

                RecordMetadata md = sender.sendWithRetry(
                        AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                        key,
                        evt.getPayload().toString()
                );

                // Segna come pubblicato; la modifica viene salvata al commit della transazione.
                evt.markPublished(Instant.ofEpochMilli(md.timestamp()));

                meterRegistry.counter(
                        AppConstants.Outbox.METRIC_OUTBOX_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                // Lasciamo l'evento non pubblicato: verrà ripreso nei cicli successivi.
            }
        }
    }
}
