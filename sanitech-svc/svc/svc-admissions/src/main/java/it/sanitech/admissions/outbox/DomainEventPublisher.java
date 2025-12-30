package it.sanitech.admissions.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.admissions.utilities.AppConstants;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Publisher di eventi di dominio verso l'Outbox.
 *
 * <p>
 * Questo componente non invia direttamente su Kafka: persistendo in outbox nella stessa transazione
 * delle modifiche di dominio, garantisce che "dato aggiornato" ed "evento" siano allineati.
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
