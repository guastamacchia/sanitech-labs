package it.sanitech.audit.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.audit.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher di eventi di dominio basato su Outbox.
 * <p>
 * Responsabilità: creare un record {@link OutboxEvent} nella stessa transazione della logica di dominio,
 * così da evitare inconsistenze tra stato DB e messaggi emessi.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            JsonNode json = objectMapper.valueToTree(payload);

            OutboxEvent evt = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .build();

            outbox.save(evt);

            meterRegistry.counter(
                    AppConstants.Outbox.METRIC_OUTBOX_SAVED,
                    AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                    AppConstants.Outbox.TAG_EVENT_TYPE, eventType
            ).increment();

        } catch (RuntimeException ex) {
            // Errore "hard": se non serializziamo il payload, non possiamo garantire consistenza.
            throw new IllegalStateException("Impossibile serializzare il payload outbox.", ex);
        }
    }
}
