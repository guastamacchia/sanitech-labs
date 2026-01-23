package it.sanitech.audit.repositories.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Evento di audit persistito su DB.
 * <p>
 * Questo servizio è pensato come "sink" centrale per:
 * <ul>
 *   <li>eventi generati via API (es. azioni utente);</li>
 *   <li>eventi consumati da Kafka (es. domain events dagli altri microservizi).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @Column(name = "actor_type", nullable = false, length = 32)
    private String actorType;

    @Column(name = "actor_id", length = 128)
    private String actorId;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @Column(name = "outcome", nullable = false, length = 32)
    private String outcome;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private JsonNode details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.occurredAt == null) {
            this.occurredAt = Instant.now();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
