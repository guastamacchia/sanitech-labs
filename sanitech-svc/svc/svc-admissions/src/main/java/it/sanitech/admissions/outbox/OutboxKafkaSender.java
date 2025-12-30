package it.sanitech.admissions.outbox;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Sender Kafka dedicato per l'Outbox.
 *
 * <p>
 * È separato dal publisher schedulato per permettere a Spring AOP di applicare
 * correttamente {@link Retry} (evitando il problema della "self-invocation").
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OutboxKafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Pubblica un singolo evento applicando retry + backoff configurati in
     * {@code resilience4j.retry.instances.outboxPublish}.
     */
    @Retry(name = "outboxPublish")
    public RecordMetadata sendWithRetry(String topic, String key, String jsonPayload) {
        try {
            return kafkaTemplate.send(topic, key, jsonPayload)
                    .get(5, TimeUnit.SECONDS)
                    .getRecordMetadata();
        } catch (Exception ex) {
            throw new KafkaException("Invio Kafka fallito per chiave=" + key, ex);
        }
    }
}
