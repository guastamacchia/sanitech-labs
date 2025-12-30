package it.sanitech.consents.outbox;

import it.sanitech.consents.utilities.AppConstants;
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
 *   <li>Tenta la pubblicazione su Kafka con retry/backoff (Resilience4j {@link Retry});</li>
 *   <li>In caso di successo, marca l’evento come pubblicato;</li>
 *   <li>Incrementa la metrica {@code outbox.events.published.count} con tag di tipo ed evento;</li>
 *   <li>In caso di fallimento, lascia l’evento non pubblicato per i tentativi futuri.</li>
 * </ul>
 * </p>
 */
@Slf4j
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

                // Qui viene marcato published=true; l'UPDATE viene scritto su DB al commit della transazione.
                evt.markPublished(Instant.ofEpochMilli(metadata.timestamp()));

                meterRegistry.counter(
                        AppConstants.Outbox.METRIC_OUTBOX_PUBLISHED,
                        AppConstants.Outbox.TAG_AGGREGATE_TYPE, evt.getAggregateType(),
                        AppConstants.Outbox.TAG_EVENT_TYPE, evt.getEventType()
                ).increment();

            } catch (Exception ex) {
                log.warn("Pubblicazione outbox fallita (id={}): {}", evt.getId(), ex.getMessage());
                // Non marchiamo l'evento come pubblicato: verrà ripreso nei cicli successivi.
            }
        }
    }

    /**
     * Pubblica un singolo evento su Kafka (con retry/backoff configurati).
     *
     * @param evt evento outbox da inviare
     * @return metadati della pubblicazione Kafka
     * @throws Exception se, dopo i retry, la pubblicazione continua a fallire
     */
    @Retry(name = "outboxPublish")
    protected RecordMetadata publishSingleWithRetry(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();
        String payload = evt.getPayload().toString();

        return kafkaTemplate
                .send(AppConstants.Outbox.TOPIC_CONSENTS_EVENTS, key, payload)
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }
}
