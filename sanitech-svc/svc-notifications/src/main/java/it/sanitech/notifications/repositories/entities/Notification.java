package it.sanitech.notifications.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entità persistente che rappresenta una notifica.
 *
 * <p>
 * Una notifica può essere:
 * <ul>
 *   <li>IN_APP: resa disponibile via API senza integrazioni esterne;</li>
 *   <li>EMAIL: inviata via SMTP e tracciata con stato e timestamp.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient", columnList = "recipient_type,recipient_id,created_at"),
                @Index(name = "idx_notifications_status", columnList = "status,created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 16)
    private RecipientType recipientType;

    @Column(name = "recipient_id", nullable = false, length = 64)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationChannel channel;

    /**
     * Destinatario tecnico (es. email).
     * Per il canale IN_APP può essere {@code null}.
     */
    @Column(name = "to_address", length = 200)
    private String toAddress;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "error_message", length = 400)
    private String errorMessage;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            // IN_APP è immediatamente disponibile; EMAIL entra in coda PENDING
            status = (channel == NotificationChannel.IN_APP) ? NotificationStatus.SENT : NotificationStatus.PENDING;
        }
        if (sentAt == null && status == NotificationStatus.SENT) {
            sentAt = Instant.now();
        }
    }

    public void markSent(Instant when) {
        this.status = NotificationStatus.SENT;
        this.sentAt = when == null ? Instant.now() : when;
        this.errorMessage = null;
    }

    public void markFailed(String error) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = error;
    }

    /**
     * Marca la notifica come letta dall'utente.
     */
    public void markRead() {
        this.status = NotificationStatus.READ;
    }

    /**
     * Archivia la notifica.
     */
    public void markArchived() {
        this.status = NotificationStatus.ARCHIVED;
    }
}
