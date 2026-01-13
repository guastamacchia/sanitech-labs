package it.sanitech.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Evento Outbox persistito a database.
 *
 * <p>
 * L'Outbox Pattern garantisce che la "scrittura a database" dell'operazione di dominio e
 * la "registrazione dell'evento" avvengano nella <b>stessa transazione</b>.
 * Un job separato pubblica poi gli eventi su Kafka e marca {@code published=true}.
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

    @Column(name = "aggregate_type", nullable = false, length = 64, updatable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64, updatable = false)
    private String eventType;

    /**
     * Payload dell'evento in formato JSON (persistito come JSONB in Postgres).
     *
     * <p>
     * Nota: {@code nullable=false} a database implica che questo campo debba essere sempre valorizzato.
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    /**
     * Timestamp di occorrenza dell'evento.
     */
    @Builder.Default
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt = Instant.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Guardrail JPA: assicura valori coerenti prima della persistenza.
     */
    @PrePersist
    private void prePersist() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        // Fail-fast: a database è NOT NULL.
        if (Objects.isNull(payload)) {
            throw new IllegalStateException("OutboxEvent non valido: payload nullo (DB column payload è NOT NULL).");
        }
    }

    /**
     * Marca l'evento come pubblicato.
     *
     * <p>
     * Questo aggiornamento viene salvato a database nella stessa transazione del job di publishing.
     * </p>
     *
     * <p>
     * Policy: idempotenza soft. Se già pubblicato, non sovrascriviamo {@code publishedAt}.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        if (this.published) {
            // già pubblicato: preserviamo lo stato esistente
            return;
        }
        this.published = true;
        this.publishedAt = Objects.nonNull(publishedAt) ? publishedAt : Instant.now();
    }
}
