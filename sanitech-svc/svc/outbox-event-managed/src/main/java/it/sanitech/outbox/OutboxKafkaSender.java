package it.sanitech.outbox;

import io.github.resilience4j.retry.annotation.Retry;
import it.sanitech.commons.utilities.AppConstants;
import it.sanitech.outbox.autoconfigure.OutboxProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Componente dedicato all'invio Kafka di un singolo evento Outbox.
 */
@Component
@RequiredArgsConstructor
public class OutboxKafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties props;

    /**
     * Invia l'evento su Kafka con retry/backoff configurati in Resilience4j.
     */
    @Retry(name = "outboxPublish")
    public RecordMetadata send(OutboxEvent evt) throws Exception {
        String topic = props.getTopic();
        if (Objects.isNull(topic) || topic.isBlank()) {
            throw new IllegalStateException("Configurazione Outbox non valida: sanitech.outbox.topic non valorizzato.");
        }
        if (Objects.isNull(evt)) {
            throw new IllegalArgumentException("OutboxEvent nullo.");
        }
        if (Objects.isNull(evt.getId())) {
            // Senza eventId stabile non puoi garantire idempotenza robusta lato consumer.
            throw new IllegalStateException("OutboxEvent non valido: id nullo (eventId mancante).");
        }

        // 1) Key Kafka = eventId univoca e stabile
        String key = evt.getId().toString();

        // 2) Value: payload JSON serializzato.
        String value = Objects.toString(evt.getPayload(), "null");

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

        // 3) Headers: facilitano dedup/observability senza dover parse-are il JSON
        record.headers().add(new RecordHeader("eventId", key.getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("aggregateType", safe(evt.getAggregateType()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("aggregateId", safe(evt.getAggregateId()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", safe(evt.getEventType()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("occurredAt", safe(String.valueOf(evt.getOccurredAt())).getBytes(StandardCharsets.UTF_8)));

        return kafkaTemplate.send(record)
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }

    private static String safe(String s) {
        return (Objects.isNull(s) || s.isBlank()) ? AppConstants.ErrorMessage.FALLBACK_VALUE : s.trim();
    }
}
