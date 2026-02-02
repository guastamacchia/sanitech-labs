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
 *
 * <p>
 * I microservizi che usano l'outbox devono includere questo package nello scan delle entity.
 * Esempio:
 * <pre>{@code
 * @EntityScan(basePackages = {
 *     "it.sanitech.tuomicroservizio.repositories.entities",
 *     OutboxEvent.ENTITY_PACKAGE
 * })
 * }</pre>
 * </p>
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

    /**
     * Package contenente le entity del modulo outbox.
     * Da usare con {@code @EntityScan} nei microservizi.
     */
    public static final String ENTITY_PACKAGE = "it.sanitech.outbox.persistence";

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
     * Topic Kafka di destinazione.
     * Se nullo, il publisher utilizzerà il topic di default configurato.
     */
    @Column(name = "topic", length = 128)
    private String topic;

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
