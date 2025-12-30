package it.sanitech.prescribing.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.prescribing.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publisher interno per scrivere eventi nella Outbox.
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper om;
    private final MeterRegistry meterRegistry;

    /**
     * Salva un evento in outbox (stessa transazione del caso d'uso chiamante).
     *
     * @param aggregateType tipo aggregato (es. "PRESCRIPTION")
     * @param aggregateId id aggregato (string)
     * @param eventType tipo evento (es. "PRESCRIPTION_CREATED")
     * @param payload payload JSON serializzabile (POJO/Map)
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Object payload) {
        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(om.valueToTree(payload))
                .published(false)
                .build();

        outbox.save(evt);

        meterRegistry.counter(
                AppConstants.Outbox.OUTBOX_EVENTS_SAVED,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, eventType
        ).increment();
    }
}
