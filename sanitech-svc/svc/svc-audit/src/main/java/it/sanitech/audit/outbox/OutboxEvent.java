package it.sanitech.audit.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento Outbox persistito su DB per garantire consegna "at-least-once" verso Kafka.
 * <p>
 * Viene creato nella stessa transazione dell'operazione di dominio e pubblicato in un secondo momento
 * da {@link OutboxKafkaPublisher}. In caso di errore di pubblicazione, l'evento resta {@code published=false}
 * e verrà ritentato nei cicli successivi.
 * </p>
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_unpublished", columnList = "published, occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 64, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64, updatable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb", updatable = false)
    private JsonNode payload;

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
     * <p>
     * Questa modifica viene salvata su DB al commit della transazione in {@link OutboxKafkaPublisher}.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        this.published = true;
        this.publishedAt = publishedAt;
    }
}
