package it.sanitech.notifications.repositories;

import it.sanitech.notifications.repositories.entities.Notification;
import it.sanitech.notifications.repositories.entities.NotificationStatus;
import it.sanitech.notifications.repositories.entities.RecipientType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Spring Data JPA per {@link Notification}.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientTypeAndRecipientId(RecipientType recipientType, String recipientId, Pageable pageable);

    /**
     * Estrae e blocca un batch di notifiche EMAIL in stato PENDING per l'invio.
     *
     * <p>
     * La clausola {@code FOR UPDATE SKIP LOCKED} consente più istanze del servizio in parallelo:
     * ogni istanza "prende" record diversi evitando doppie elaborazioni.
     * </p>
     */
    @Query(value = """
            SELECT *
            FROM notifications
            WHERE status = 'PENDING'
              AND channel = 'EMAIL'
            ORDER BY created_at ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Notification> lockPendingEmailBatch(@Param("limit") int limit);

    long countByStatus(NotificationStatus status);
}
