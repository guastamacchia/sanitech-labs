package it.sanitech.outbox;

import it.sanitech.commons.utilities.AppConstants;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Componente dedicato all'invio Kafka di un singolo evento Outbox.
 *
 * <p>
 * Separare l'invio in un bean distinto evita il problema della "self-invocation":
 * l'annotazione {@link Retry} viene applicata correttamente dal proxy Spring.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OutboxKafkaSender {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Invia l'evento su Kafka con retry/backoff configurati in Resilience4j.
     */
    @Retry(name = "outboxPublish")
    public RecordMetadata send(OutboxEvent evt) throws Exception {
        String key = evt.getAggregateType() + ":" + evt.getAggregateId();

        return kafkaTemplate.send(
                        AppConstants.Outbox.TOPIC_DIRECTORY_EVENTS,
                        key,
                        evt.getPayload().toString()
                )
                .get(5, TimeUnit.SECONDS)
                .getRecordMetadata();
    }
}
