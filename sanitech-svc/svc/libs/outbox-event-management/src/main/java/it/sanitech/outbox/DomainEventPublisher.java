package it.sanitech.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.commons.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/**
 * Publisher di eventi di dominio (Outbox Pattern).
 *
 * <p>
 * Questo componente non invia direttamente su Kafka: registra un {@link OutboxEvent}
 * nella stessa transazione dell'operazione di dominio. La consegna su Kafka è demandata
 * al job {@link OutboxKafkaPublisher}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    /**
     * Registra un evento nella tabella outbox.
     *
     * <p>
     * Policy:
     * <ul>
     *   <li>fail-fast su identificativi invalidi (evita eventi "sporchi" e metriche ad alta cardinalità)</li>
     *   <li>payload null → JSON null node (evento valido, ma payload assente)</li>
     * </ul>
     * </p>
     *
     * @param aggregateType tipo aggregato (es. DOCTOR, PATIENT)
     * @param aggregateId   id aggregato
     * @param eventType     tipo evento (es. DOCTOR_CREATED)
     * @param payload       payload evento (verrà serializzato a JSON)
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        String safeAggregateType = requireText(aggregateType, "aggregateType");
        String safeAggregateId = requireText(aggregateId, "aggregateId");
        String safeEventType = requireText(eventType, "eventType");

        // valueToTree accetta null e produce un NullNode: esplicitiamo la scelta.
        JsonNode jsonPayload = objectMapper.valueToTree(Objects.nonNull(payload) ? payload : Map.of());

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType(safeAggregateType)
                .aggregateId(safeAggregateId)
                .eventType(safeEventType)
                .payload(jsonPayload)
                .build();

        outbox.save(evt);

        // Metriche: contatore per eventi salvati con tag stabili (evita spazi/casing incoerenti).
        meterRegistry.counter(
                AppConstants.Outbox.OUTBOX_EVENTS_SAVED_COUNT,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, safeAggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, safeEventType
        ).increment();
    }

    /**
     * Richiede una stringa non nulla e non blank; ritorna la versione trim-mata.
     *
     * <p>
     * Fail-fast: registrare un outbox event con campi vuoti è quasi sempre un errore di programmazione.
     * </p>
     */
    private static String requireText(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException("Outbox event non valido: " + fieldName + " nullo o vuoto.");
        }
        return value.trim();
    }
}
