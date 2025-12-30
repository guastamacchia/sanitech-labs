package it.sanitech.televisit.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Evento Outbox persistito su DB per garantire consegna affidabile verso Kafka.
 *
 * <p>L'evento viene creato nella <strong>stessa transazione</strong> dell'operazione di dominio,
 * e pubblicato in modo asincrono da {@link OutboxKafkaPublisher}.</p>
 */
@Entity
@Table(name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
     * Payload serializzato (JSON) e salvato su colonna JSONB.
     */
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    /**
     * Marca l'evento come pubblicato: il valore viene salvato su DB al commit della transazione.
     */
    public void markPublished(Instant when) {
        this.published = true;
        this.publishedAt = when != null ? when : Instant.now();
    }
}
