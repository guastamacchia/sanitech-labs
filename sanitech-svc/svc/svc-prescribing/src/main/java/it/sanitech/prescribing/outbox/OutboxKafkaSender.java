package it.sanitech.prescribing.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.prescribing.utilities.AppConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Componente dedicato all'invio su Kafka di un singolo evento outbox.
 *
 * <p>
 * È separato da {@link OutboxKafkaPublisher} per evitare self-invocation e garantire
 * l'applicazione di {@link Retry} via Spring AOP.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OutboxKafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper om;

    @Retry(name = "outboxPublish")
    public RecordMetadata publish(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();
        String jsonPayload = toJson(evt);

        return kafkaTemplate.send(AppConstants.Outbox.TOPIC_PRESCRIBING_EVENTS, key, jsonPayload)
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }

    private String toJson(OutboxEvent evt) throws JsonProcessingException {
        return om.writeValueAsString(evt.getPayload());
    }
}
