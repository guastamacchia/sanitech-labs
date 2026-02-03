package it.sanitech.outbox.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import it.sanitech.outbox.persistence.OutboxEvent;
import it.sanitech.outbox.persistence.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

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
     *
     * @deprecated Usare {@link #publish(String, String, String, Object, String, ActorInfo)} per tracciare l'attore
     */
    @Deprecated
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload) {
        publish(aggregateType, aggregateId, eventType, payload, null, ActorInfo.SYSTEM);
    }

    /**
     * Registra un evento outbox con topic specifico.
     *
     * @param aggregateType tipo aggregato (es. DOCTOR, PATIENT)
     * @param aggregateId   identificativo aggregato
     * @param eventType     tipo evento (es. DOCTOR_CREATED)
     * @param payload       payload dell'evento
     * @param topic         topic Kafka di destinazione (se null, usa il default)
     *
     * @deprecated Usare {@link #publish(String, String, String, Object, String, ActorInfo)} per tracciare l'attore
     */
    @Deprecated
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload,
                        String topic) {
        publish(aggregateType, aggregateId, eventType, payload, topic, ActorInfo.SYSTEM);
    }

    /**
     * Registra un evento outbox con informazioni complete sull'attore.
     *
     * @param aggregateType tipo aggregato (es. DOCTOR, PATIENT)
     * @param aggregateId   identificativo aggregato
     * @param eventType     tipo evento (es. DOCTOR_CREATED)
     * @param payload       payload dell'evento
     * @param topic         topic Kafka di destinazione (se null, usa il default)
     * @param actor         informazioni sull'attore che ha generato l'evento
     */
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload,
                        String topic,
                        ActorInfo actor) {

        ActorInfo effectiveActor = actor != null ? actor : ActorInfo.SYSTEM;

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(Objects.requireNonNull(aggregateType, "aggregateType obbligatorio"));
        event.setAggregateId(Objects.requireNonNull(aggregateId, "aggregateId obbligatorio"));
        event.setEventType(Objects.requireNonNull(eventType, "eventType obbligatorio"));
        event.setTopic(topic);
        event.setActorType(effectiveActor.actorType());
        event.setActorId(effectiveActor.actorId());
        event.setActorName(effectiveActor.actorName());
        event.setPayload(Objects.isNull(payload)
                ? JsonNodeFactory.instance.objectNode()
                : objectMapper.valueToTree(payload));

        publish(event);
    }

    /**
     * Registra un evento outbox estraendo automaticamente le informazioni dell'attore
     * dall'oggetto Authentication di Spring Security.
     *
     * @param aggregateType tipo aggregato (es. DOCTOR, PATIENT)
     * @param aggregateId   identificativo aggregato
     * @param eventType     tipo evento (es. DOCTOR_CREATED)
     * @param payload       payload dell'evento
     * @param topic         topic Kafka di destinazione (se null, usa il default)
     * @param auth          Authentication da cui estrarre le informazioni dell'attore
     */
    public void publish(String aggregateType,
                        String aggregateId,
                        String eventType,
                        Object payload,
                        String topic,
                        Authentication auth) {
        publish(aggregateType, aggregateId, eventType, payload, topic, ActorInfo.from(auth));
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
