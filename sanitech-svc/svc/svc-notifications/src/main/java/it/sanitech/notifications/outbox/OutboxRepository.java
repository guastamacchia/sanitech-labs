package it.sanitech.notifications.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository per la tabella outbox.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Estrae e blocca un batch di eventi non pubblicati.
     *
     * <p>
     * {@code SKIP LOCKED} evita contention e consente più istanze del publisher in parallelo.
     * </p>
     */
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published = false
            ORDER BY occurred_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockBatch();
}
