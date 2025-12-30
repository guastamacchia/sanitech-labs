package it.sanitech.docs.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.docs.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher di eventi di dominio verso Outbox.
 *
 * <p>
 * Questo componente non invia direttamente a Kafka: salva un record in {@link OutboxEvent}.
 * La pubblicazione su Kafka avviene in un job schedulato (vedi {@link OutboxKafkaPublisher}).
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    /**
     * Aggiunge un evento in outbox nella transazione corrente.
     *
     * @param aggregateType tipo aggregato (es. DOCUMENT)
     * @param aggregateId id aggregato (string)
     * @param eventType tipo evento (es. DOCUMENT_UPLOADED)
     * @param payload payload JSON-serializzabile
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            OutboxEvent evt = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .build();

            outbox.save(evt);

            meterRegistry.counter(
                    AppConstants.Outbox.OUTBOX_EVENTS_SAVED,
                    AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                    AppConstants.Outbox.TAG_EVENT_TYPE, eventType
            ).increment();

        } catch (Exception e) {
            throw new IllegalStateException("Impossibile serializzare il payload Outbox.", e);
        }
    }
}
