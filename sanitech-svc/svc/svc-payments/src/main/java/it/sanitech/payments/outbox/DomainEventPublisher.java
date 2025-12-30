package it.sanitech.payments.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import it.sanitech.payments.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher applicativo per scrivere eventi di dominio nella Outbox.
 *
 * <p>
 * Questo componente NON pubblica direttamente su Kafka: inserisce un record nella tabella Outbox
 * nella stessa transazione del comando di dominio (garanzia "transactional outbox").
 * La pubblicazione reale verso Kafka è demandata a {@link OutboxKafkaPublisher}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    /**
     * Registra un evento nella Outbox (stessa transazione del chiamante).
     */
    @Transactional
    public void add(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        String json = toJson(payload);

        OutboxEvent evt = OutboxEvent.newUnpublished(aggregateType, aggregateId, eventType, json);
        outbox.save(evt);

        meterRegistry.counter(
                AppConstants.Outbox.OUTBOX_EVENTS_SAVED_COUNT,
                AppConstants.Outbox.TAG_AGGREGATE_TYPE, aggregateType,
                AppConstants.Outbox.TAG_EVENT_TYPE, eventType
        ).increment();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossibile serializzare il payload dell'evento outbox.", e);
        }
    }
}
