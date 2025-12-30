package it.sanitech.payments.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository Outbox.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Estrae un batch di eventi non pubblicati con lock pessimista.
     *
     * <p>
     * {@code FOR UPDATE SKIP LOCKED} significa:
     * - prende un lock sulle righe selezionate (FOR UPDATE),
     * - se un'altra transazione sta già processando quelle righe, le salta (SKIP LOCKED),
     * evitando duplicazioni quando ci sono più istanze/pod del servizio.
     * </p>
     */
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published = false
            ORDER BY occurred_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockBatch(int limit);
}
