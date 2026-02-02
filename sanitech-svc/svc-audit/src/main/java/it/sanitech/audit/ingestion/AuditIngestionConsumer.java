package it.sanitech.audit.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.audit.repositories.entities.AuditEvent;
import it.sanitech.audit.repositories.AuditEventRepository;
import it.sanitech.audit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumer Kafka per ingestion di eventi da altri microservizi.
 * <p>
 * Abilitabile/disabilitabile via property:
 * {@code sanitech.audit.ingestion.enabled}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.audit.ingestion", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditIngestionConsumer {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.audit.ingestion.topics}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode details = parse(record.value());

            repository.save(AuditEvent.builder()
                    .occurredAt(Instant.now())
                    .source(AppConstants.Audit.SOURCE_KAFKA)
                    .actorType("SYSTEM")
                    .actorId("kafka:" + record.topic())
                    .action("INGEST_EVENT")
                    .resourceType(record.topic())
                    .resourceId(record.key())
                    .outcome(AppConstants.Audit.OUTCOME_SUCCESS)
                    .details(details)
                    .build());

        } catch (Exception ex) {
            // Ingestion non deve bloccare il consumer: logghiamo e proseguiamo.
            log.warn("Ingestion audit fallita (topic={}, offset={}): {}", record.topic(), record.offset(), ex.getMessage());
        }
    }

    private JsonNode parse(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception ignored) {
            return objectMapper.createObjectNode().put("raw", raw);
        }
    }
}
