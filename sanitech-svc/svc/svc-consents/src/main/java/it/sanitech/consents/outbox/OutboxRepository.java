package it.sanitech.consents.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository per la tabella {@code outbox_events}.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Recupera e "blocca" un batch di eventi non pubblicati.
     * <p>
     * {@code FOR UPDATE SKIP LOCKED} permette a più istanze/pod di lavorare in parallelo:
     * ogni istanza prende un insieme di righe senza contendersi le stesse (quelle già bloccate vengono saltate).
     * </p>
     */
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE published = false
            ORDER BY occurred_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<OutboxEvent> lockBatch(@Param("limit") int limit);
}
