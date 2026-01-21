package it.sanitech.outbox.publisher;

import it.sanitech.outbox.persistence.OutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Sender di default basato su KafkaTemplate.
 */
@Slf4j
public class DefaultOutboxKafkaSender implements OutboxKafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DefaultOutboxKafkaSender(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "KafkaTemplate obbligatorio");
    }

    @Override
    public void sendSync(String topic, OutboxEvent event, long timeoutMs) {
        Objects.requireNonNull(event, "OutboxEvent obbligatorio");
        String key = event.getId() != null ? event.getId().toString() : null;
        String payload = event.getPayload() != null ? event.getPayload().toString() : "{}";

        try {
            kafkaTemplate.send(topic, key, payload).get(timeoutMs, TimeUnit.MILLISECONDS);
            log.debug("Outbox: evento {} inviato su topic='{}' (key='{}').", event.getId(), topic, key);
        } catch (Exception ex) {
            throw new IllegalStateException("Outbox: invio Kafka fallito per evento " + event.getId()
                    + " su topic '" + topic + "'.", ex);
        }
    }
}
