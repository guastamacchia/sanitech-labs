package it.sanitech.outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper objectMapper;

    public DefaultOutboxKafkaSender(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "KafkaTemplate obbligatorio");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper obbligatorio");
    }

    @Override
    public void sendSync(String topic, OutboxEvent event, long timeoutMs) {
        Objects.requireNonNull(event, "OutboxEvent obbligatorio");
        String key = event.getId() != null ? event.getId().toString() : null;
        String message = buildEnvelope(event);

        try {
            kafkaTemplate.send(topic, key, message).get(timeoutMs, TimeUnit.MILLISECONDS);
            log.debug("Outbox: evento {} inviato su topic='{}' (key='{}').", event.getId(), topic, key);
        } catch (Exception ex) {
            throw new IllegalStateException("Outbox: invio Kafka fallito per evento " + event.getId()
                    + " su topic '" + topic + "'.", ex);
        }
    }

    private String buildEnvelope(OutboxEvent event) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("aggregateType", event.getAggregateType());
        envelope.put("aggregateId", event.getAggregateId());
        envelope.put("eventType", event.getEventType());

        // Aggiungi informazioni sull'attore per il tracciamento audit
        ObjectNode actor = objectMapper.createObjectNode();
        actor.put("type", event.getActorType() != null ? event.getActorType() : "SYSTEM");
        actor.put("id", event.getActorId() != null ? event.getActorId() : "system");
        actor.put("name", event.getActorName());
        envelope.set("actor", actor);

        // Timestamp originale dell'evento
        envelope.put("occurredAt", event.getOccurredAt() != null
                ? event.getOccurredAt().toString()
                : null);

        envelope.set("payload", event.getPayload() != null
                ? event.getPayload()
                : objectMapper.createObjectNode());

        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Errore serializzazione envelope outbox per evento " + event.getId(), ex);
        }
    }
}
