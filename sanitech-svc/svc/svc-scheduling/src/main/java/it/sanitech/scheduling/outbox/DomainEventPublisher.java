package it.sanitech.scheduling.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
     * @param aggregateType tipo aggregato (es. SLOT, APPOINTMENT)
     * @param aggregateId   id aggregato
     * @param eventType     tipo evento (es. SLOT_CREATED)
     * @param payload       payload evento (verrà serializzato a JSON)
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        JsonNode jsonPayload = objectMapper.valueToTree(payload);

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(jsonPayload)
                .build();

        outbox.save(evt);

        meterRegistry.counter(
                AppConstants.Outbox.OUTBOX_EVENTS_SAVED_COUNT,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, eventType
        ).increment();
    }
}
