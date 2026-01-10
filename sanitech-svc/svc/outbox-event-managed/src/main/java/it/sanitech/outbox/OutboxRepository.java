package it.sanitech.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository per {@link OutboxEvent}.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Seleziona e blocca un batch di eventi non pubblicati.
     *
     * <p>
     * {@code FOR UPDATE SKIP LOCKED} permette concorrenti publisher: ogni istanza prende
     * un sottoinsieme di righe senza contention (le righe già lockate vengono "saltate").
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
    List<OutboxEvent> lockBatch(@Param("limit") int limit);
}
