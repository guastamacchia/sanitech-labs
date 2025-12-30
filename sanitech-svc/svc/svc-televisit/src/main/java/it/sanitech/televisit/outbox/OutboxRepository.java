package it.sanitech.televisit.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Outbox.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Estrae un batch di eventi non pubblicati usando lock pessimista.
     *
     * <p><strong>FOR UPDATE SKIP LOCKED</strong>:
     * evita che due istanze elaborino lo stesso record contemporaneamente, saltando
     * le righe già lockate da altri worker.</p>
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
