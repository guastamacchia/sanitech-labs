package it.sanitech.payments.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Evento Outbox (pattern transactional outbox).
 *
 * <p>
 * Il record viene inserito nella stessa transazione dell'operazione di dominio (create/update/...)
 * per garantire la consegna degli eventi anche in presenza di retry o crash del servizio.
 * </p>
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
})
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
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

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();

    @Column(name = "published", nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Marca l'evento come pubblicato.
     */
    public void markPublished(Instant when) {
        this.published = true;
        this.publishedAt = when;
    }

    public static OutboxEvent newUnpublished(String aggregateType, String aggregateId, String eventType, String payloadJson) {
        return OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadJson)
                .published(false)
                .build();
    }
}
