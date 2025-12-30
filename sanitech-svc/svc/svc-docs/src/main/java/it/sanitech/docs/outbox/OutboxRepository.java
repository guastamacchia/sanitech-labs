package it.sanitech.docs.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Outbox con lock pessimista per prelevare batch di eventi non pubblicati.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Estrae un batch di eventi non pubblicati in modo concorrente-safe.
     *
     * <p>
     * {@code FOR UPDATE SKIP LOCKED} consente a più istanze di publisher di lavorare in parallelo:
     * ogni istanza "blocca" righe diverse e salta quelle già bloccate da altre.
     * </p>
     */
    @Query(value =
            "SELECT * FROM outbox_events " +
            "WHERE published = false " +
            "ORDER BY occurred_at " +
            "LIMIT :limit " +
            "FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEvent> lockBatch(@Param("limit") int limit);
}
