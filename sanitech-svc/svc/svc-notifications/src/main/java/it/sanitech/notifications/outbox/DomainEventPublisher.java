package it.sanitech.notifications.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.notifications.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher applicativo che salva eventi nella outbox.
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    /**
     * Salva un evento nella outbox in modo transazionale.
     *
     * @param aggregateType tipo aggregato (es. NOTIFICATION)
     * @param aggregateId   id aggregato
     * @param eventType     tipo evento (es. NOTIFICATION_CREATED)
     * @param payload       payload JSON serializzabile
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        OutboxEvent evt = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(objectMapper.valueToTree(payload))
                .published(false)
                .build();

        outbox.save(evt);

        meterRegistry.counter(
                AppConstants.Outbox.METRIC_OUTBOX_SAVED,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, eventType
        ).increment();
    }
}
