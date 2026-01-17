package it.sanitech.outbox.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository Outbox.
 *
 * Nota fondamentale: lockBatch usa FOR UPDATE SKIP LOCKED e deve essere invocato DENTRO transazione.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = "SELECT * " +
            "FROM outbox_events " +
            "WHERE published_at IS NULL " +
            "ORDER BY created_at " +
            "LIMIT :batchSize " +
            "FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> lockBatch(@Param("batchSize") int batchSize);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE outbox_events " +
            "SET published_at = :publishedAt " +
            "WHERE id IN (:ids)", nativeQuery = true)
    void markPublished(@Param("ids") List<UUID> ids, @Param("publishedAt") Instant publishedAt);
}
