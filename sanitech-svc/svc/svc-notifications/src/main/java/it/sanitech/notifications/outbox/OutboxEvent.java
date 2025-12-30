package it.sanitech.notifications.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento Outbox persistito su database.
 *
 * <p>
 * Implementa il pattern "Transactional Outbox": l'evento viene salvato nella stessa transazione
 * dell'operazione applicativa; un job schedulato lo pubblica su Kafka e lo marca come pubblicato.
 * </p>
 */
@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    /**
     * Payload JSON serializzato su colonna JSONB.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void prePersist() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public void markPublished(Instant when) {
        this.published = true;
        this.publishedAt = when == null ? Instant.now() : when;
    }
}
