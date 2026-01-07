package it.sanitech.directory.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento Outbox persistito a DB.
 *
 * <p>
 * L'Outbox Pattern garantisce che la "scrittura a DB" dell'operazione di dominio e
 * la "registrazione dell'evento" avvengano nella <b>stessa transazione</b>.
 * Un job separato pubblica poi gli eventi su Kafka e marca {@code published=true}.
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
     * {@link JdbcTypeCode} forza Hibernate ad usare il tipo JDBC JSON, evitando problemi
     * di validazione schema e consentendo lettura/scrittura nativa su Postgres JSONB.
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Builder.Default
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt = Instant.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Marca l'evento come pubblicato.
     *
     * <p>
     * Questo aggiornamento viene salvato a DB nella stessa transazione del job di publishing.
     * </p>
     */
    public void markPublished(Instant publishedAt) {
        this.published = true;
        this.publishedAt = publishedAt != null ? publishedAt : Instant.now();
    }
}
