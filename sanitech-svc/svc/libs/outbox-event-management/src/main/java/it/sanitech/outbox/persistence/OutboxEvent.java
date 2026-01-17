package it.sanitech.outbox.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Entità Outbox (pensata per PostgreSQL: jsonb + SKIP LOCKED).
 */
@Getter
@Setter
@Entity
@Table(name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_event_published_at", columnList = "published_at"),
                @Index(name = "idx_outbox_event_created_at", columnList = "created_at")
        })
public class OutboxEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    /**
     * Payload JSON (jsonb).
     * Policy: non nullo. Se mancante, viene forzato a "{}".
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
