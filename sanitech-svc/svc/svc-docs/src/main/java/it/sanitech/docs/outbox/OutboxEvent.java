package it.sanitech.docs.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento Outbox persistito su Postgres per consegna affidabile via Kafka.
 *
 * <p>
 * Pattern Outbox: l'evento viene scritto nella <strong>stessa transazione</strong> della modifica di dominio,
 * poi un publisher schedulato lo invia su Kafka e lo marca come pubblicato.
 * </p>
 */
@Entity
@Table(name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
        })
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
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Builder.Default
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt = Instant.now();

    @Builder.Default
    @Column(name = "published", nullable = false)
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Marca l'evento come pubblicato.
     *
     * <p>
     * Poiché l'entità è gestita da JPA dentro una transazione, l'update verrà persistito a commit.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        this.published = true;
        this.publishedAt = publishedAt;
    }
}
