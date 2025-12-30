package it.sanitech.admissions.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Outbox.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Estrae un batch di eventi non pubblicati bloccando le righe selezionate.
     *
     * <p>
     * {@code FOR UPDATE SKIP LOCKED} significa:
     * <ul>
     *   <li>{@code FOR UPDATE}: blocca le righe selezionate per evitare che altre transazioni le processino;</li>
     *   <li>{@code SKIP LOCKED}: se una riga è già bloccata da un altro worker, viene saltata (no attesa).</li>
     * </ul>
     * </p>
     */
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published = false
            ORDER BY occurred_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockBatch(@Param("limit") int limit);
}
