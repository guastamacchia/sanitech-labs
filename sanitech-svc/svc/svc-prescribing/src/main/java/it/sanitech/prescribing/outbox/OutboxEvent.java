package it.sanitech.prescribing.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento Outbox persistito su DB per garantire consegna affidabile verso Kafka.
 *
 * <p>
 * Pattern: l'evento viene scritto nella stessa transazione della modifica di dominio;
 * un job schedulato provvede poi alla pubblicazione su Kafka e marca l'evento come pubblicato.
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
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        occurredAt = (occurredAt == null) ? Instant.now() : occurredAt;
    }

    /**
     * Marca l'evento come pubblicato.
     *
     * <p>
     * Chiamato dal publisher dopo l'ack di Kafka. Essendo l'entità gestita da JPA, il flag
     * viene sincronizzato su DB al commit della transazione.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        this.published = true;
        this.publishedAt = (publishedAt == null) ? Instant.now() : publishedAt;
    }
}
