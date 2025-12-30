package it.sanitech.prescribing.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository per la tabella outbox.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Seleziona un batch di eventi non pubblicati, bloccandoli per evitare che più istanze
     * pubblichino lo stesso evento.
     *
     * <p>
     * La clausola {@code FOR UPDATE SKIP LOCKED} permette un pattern "work stealing"
     * tra più repliche: ogni replica prende solo righe non già lockate.
     * </p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            SELECT * FROM outbox_events
            WHERE published = false
            ORDER BY occurred_at
            LIMIT :size
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockBatch(@Param("size") int size);
}
