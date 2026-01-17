package it.sanitech.outbox.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Entità Outbox (pensata per PostgreSQL: jsonb + SKIP LOCKED).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
        })
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /**
     * Payload JSON (jsonb).
     * Policy: non nullo. Se mancante, viene forzato a "{}".
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private JsonNode payload = JsonNodeFactory.instance.objectNode();

    @Column(name = "occurred_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(name = "published", nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void ensureDefaults() {
        if (payload == null) {
            payload = JsonNodeFactory.instance.objectNode();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public static OutboxEvent newUnpublished(String aggregateType,
                                             String aggregateId,
                                             String eventType,
                                             String payloadJson) {
        JsonNode payloadNode = null;
        if (payloadJson != null && !payloadJson.isBlank()) {
            try {
                payloadNode = new ObjectMapper().readTree(payloadJson);
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException("Payload JSON non valido per outbox event.", ex);
            }
        }

        return OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadNode == null ? JsonNodeFactory.instance.objectNode() : payloadNode)
                .published(false)
                .build();
    }
}
