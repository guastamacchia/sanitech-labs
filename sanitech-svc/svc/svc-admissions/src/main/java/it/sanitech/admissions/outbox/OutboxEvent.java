package it.sanitech.admissions.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento tecnico persistito in tabella Outbox per garantire consegna affidabile verso Kafka.
 *
 * <p>
 * L'evento viene creato nella stessa transazione della modifica di dominio
 * e pubblicato asincronamente dal job {@link OutboxKafkaPublisher}.
 * </p>
 */
@Entity
@Table(name = "outbox_events",
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at", columnDefinition = "timestamptz")
    private Instant publishedAt;

    /**
     * Marca l'evento come pubblicato.
     *
     * <p>
     * In JPA il cambiamento viene persistito a fine transazione (dirty-checking)
     * oppure esplicitamente tramite {@code save()} del repository.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        this.published = true;
        this.publishedAt = publishedAt;
    }

    @PrePersist
    void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
