package it.sanitech.audit.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.audit.repositories.entities.AuditEvent;
import it.sanitech.audit.repositories.AuditEventRepository;
import it.sanitech.audit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumer Kafka per ingestion di eventi da altri microservizi.
 * <p>
 * Abilitabile/disabilitabile via property:
 * {@code sanitech.audit.ingestion.enabled}.
 * </p>
 * <p>
 * L'envelope Kafka atteso ha la seguente struttura:
 * <pre>{@code
 * {
 *   "aggregateType": "DOCTOR",
 *   "aggregateId": "123",
 *   "eventType": "DOCTOR_CREATED",
 *   "actor": {
 *     "type": "ADMIN",
 *     "id": "admin@sanitech.it",
 *     "name": "Admin User"
 *   },
 *   "occurredAt": "2026-02-03T10:00:00Z",
 *   "payload": { ... }
 * }
 * }</pre>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.audit.ingestion", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditIngestionConsumer {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.audit.ingestion.topics}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode envelope = parse(record.value());

            // Estrai informazioni dall'envelope
            String eventType = extractEventType(envelope);
            String aggregateType = extractAggregateType(envelope);
            String aggregateId = extractAggregateId(envelope);
            Instant occurredAt = extractOccurredAt(envelope);

            // Estrai informazioni sull'attore
            String actorType = extractActorType(envelope);
            String actorId = extractActorId(envelope);

            // Costruisci l'azione leggibile dal tipo evento
            String action = buildAction(eventType, aggregateType);

            // Costruisci il resourceType dall'aggregateType
            String resourceType = aggregateType != null ? aggregateType : record.topic();

            // Costruisci il resourceId dall'aggregateId
            String resourceId = aggregateId != null ? aggregateId : record.key();

            repository.save(AuditEvent.builder()
                    .occurredAt(occurredAt)
                    .source(AppConstants.Audit.SOURCE_KAFKA)
                    .actorType(actorType)
                    .actorId(actorId)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .outcome(AppConstants.Audit.OUTCOME_SUCCESS)
                    .details(envelope)
                    .build());

            log.debug("Audit event ingested: action={}, actorId={}, resourceType={}, resourceId={}",
                    action, actorId, resourceType, resourceId);

        } catch (Exception ex) {
            // Ingestion non deve bloccare il consumer: logghiamo e proseguiamo.
            log.warn("Ingestion audit fallita (topic={}, offset={}): {}", record.topic(), record.offset(), ex.getMessage());
        }
    }

    /**
     * Estrae il tipo di evento dall'envelope.
     */
    private String extractEventType(JsonNode envelope) {
        if (envelope == null) return null;
        JsonNode node = envelope.get("eventType");
        return node != null && !node.isNull() ? node.asText() : null;
    }

    /**
     * Estrae il tipo di aggregato dall'envelope.
     */
    private String extractAggregateType(JsonNode envelope) {
        if (envelope == null) return null;
        JsonNode node = envelope.get("aggregateType");
        return node != null && !node.isNull() ? node.asText() : null;
    }

    /**
     * Estrae l'ID dell'aggregato dall'envelope.
     */
    private String extractAggregateId(JsonNode envelope) {
        if (envelope == null) return null;
        JsonNode node = envelope.get("aggregateId");
        return node != null && !node.isNull() ? node.asText() : null;
    }

    /**
     * Estrae il timestamp originale dell'evento.
     */
    private Instant extractOccurredAt(JsonNode envelope) {
        if (envelope == null) return Instant.now();
        JsonNode node = envelope.get("occurredAt");
        if (node != null && !node.isNull()) {
            try {
                return Instant.parse(node.asText());
            } catch (Exception e) {
                log.debug("Impossibile parsare occurredAt: {}", node.asText());
            }
        }
        return Instant.now();
    }

    /**
     * Estrae il tipo di attore dall'envelope.
     * Cerca in envelope.actor.type, altrimenti fallback a SYSTEM.
     */
    private String extractActorType(JsonNode envelope) {
        if (envelope == null) return "SYSTEM";

        JsonNode actor = envelope.get("actor");
        if (actor != null && !actor.isNull()) {
            JsonNode typeNode = actor.get("type");
            if (typeNode != null && !typeNode.isNull()) {
                return typeNode.asText();
            }
        }

        return "SYSTEM";
    }

    /**
     * Estrae l'ID dell'attore dall'envelope.
     * Cerca in envelope.actor.id, altrimenti fallback a "system".
     */
    private String extractActorId(JsonNode envelope) {
        if (envelope == null) return "system";

        JsonNode actor = envelope.get("actor");
        if (actor != null && !actor.isNull()) {
            JsonNode idNode = actor.get("id");
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }

        return "system";
    }

    /**
     * Costruisce un'azione leggibile dal tipo evento.
     * Es: DOCTOR_CREATED -> "Creazione medico"
     */
    private String buildAction(String eventType, String aggregateType) {
        if (eventType == null) return "UNKNOWN";

        // Usa direttamente l'eventType come action (es. DOCTOR_CREATED, PATIENT_UPDATED)
        // Questo permette di filtrare e cercare per tipo evento
        return eventType;
    }

    private JsonNode parse(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception ignored) {
            return objectMapper.createObjectNode().put("raw", raw);
        }
    }
}
