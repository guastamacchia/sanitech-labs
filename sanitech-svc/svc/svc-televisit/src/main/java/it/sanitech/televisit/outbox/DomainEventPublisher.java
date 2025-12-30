package it.sanitech.televisit.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.televisit.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher di eventi di dominio verso Outbox (persistenza).
 *
 * <p>Salva l'evento in tabella Outbox nella stessa transazione del caso d'uso.
 * Il flush verso Kafka è demandato a {@link OutboxKafkaPublisher}.</p>
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
     * @param aggregateType tipo aggregato (es. TELEVISIT_SESSION)
     * @param aggregateId   id aggregato
     * @param eventType     tipo evento (es. CREATED/STARTED/ENDED)
     * @param payload       payload business (serializzato JSON)
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        String json = toJson(payload);

        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(json)
                .published(false)
                .build();

        outbox.save(evt);

        meterRegistry.counter(
                AppConstants.Outbox.METRIC_EVENTS_SAVED,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, eventType
        ).increment();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Impossibile serializzare il payload outbox.", e);
        }
    }
}
