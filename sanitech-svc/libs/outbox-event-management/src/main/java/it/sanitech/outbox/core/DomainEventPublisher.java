package it.sanitech.outbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * API per registrare eventi nel DB outbox.
 *
 * <p>
 * Responsabilità:
 * - persistere eventi su tabella outbox
 * - NON pubblicare su Kafka
 * La pubblicazione asincrona è demandata al job schedulato
 * {@code OutboxKafkaPublisher}.
 * </p>
 */
@Slf4j
public final class DomainEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public DomainEventPublisher(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxRepository obbligatorio");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper obbligatorio");
    }

    /**
     * Registra un evento outbox partendo dai singoli campi.
     * Usa il topic di default configurato nel microservizio.
     */
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload) {
        publish(aggregateType, aggregateId, eventType, payload, null);
    }

    /**
     * Registra un evento outbox con topic specifico.
     *
     * @param aggregateType tipo aggregato (es. DOCTOR, PATIENT)
     * @param aggregateId   identificativo aggregato
     * @param eventType     tipo evento (es. DOCTOR_CREATED)
     * @param payload       payload dell'evento
     * @param topic         topic Kafka di destinazione (se null, usa il default)
     */
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload,
                        String topic) {

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(Objects.requireNonNull(aggregateType, "aggregateType obbligatorio"));
        event.setAggregateId(Objects.requireNonNull(aggregateId, "aggregateId obbligatorio"));
        event.setEventType(Objects.requireNonNull(eventType, "eventType obbligatorio"));
        event.setTopic(topic);
        event.setPayload(Objects.isNull(payload)
                ? JsonNodeFactory.instance.objectNode()
                : objectMapper.valueToTree(payload));

        publish(event);
    }

    /**
     * Variante per casi avanzati in cui l'evento è già costruito.
     */
    public void publish(OutboxEvent event) {
        Objects.requireNonNull(event, "OutboxEvent obbligatorio");

        if (event.getPayload() == null) {
            // Policy: payload mai nullo
            event.setPayload(JsonNodeFactory.instance.objectNode());
        }

        OutboxEvent saved = outboxRepository.save(event);

        log.debug("Outbox: evento salvato su DB. id={}, aggregateType={}, eventType={}, topic={}",
                saved.getId(), saved.getAggregateType(), saved.getEventType(), saved.getTopic());

    }
}
