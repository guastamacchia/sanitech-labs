package it.sanitech.directory.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entità per le preferenze di notifica del paziente.
 *
 * <p>
 * Ogni paziente può configurare quali canali di notifica (email/SMS) desidera
 * utilizzare per le diverse categorie di comunicazioni:
 * <ul>
 *   <li>Promemoria appuntamenti</li>
 *   <li>Nuovi documenti clinici</li>
 *   <li>Pagamenti e fatture</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Paziente associato (relazione 1:1). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private Patient patient;

    // ========== Promemoria appuntamenti ==========

    /** Ricevi promemoria appuntamenti via email. */
    @Column(name = "email_reminders", nullable = false)
    @Builder.Default
    private boolean emailReminders = true;

    /** Ricevi promemoria appuntamenti via SMS. */
    @Column(name = "sms_reminders", nullable = false)
    @Builder.Default
    private boolean smsReminders = false;

    // ========== Nuovi documenti clinici ==========

    /** Ricevi notifica nuovi documenti clinici via email. */
    @Column(name = "email_documents", nullable = false)
    @Builder.Default
    private boolean emailDocuments = true;

    /** Ricevi notifica nuovi documenti clinici via SMS. */
    @Column(name = "sms_documents", nullable = false)
    @Builder.Default
    private boolean smsDocuments = false;

    // ========== Pagamenti e fatture ==========

    /** Ricevi notifica pagamenti e fatture via email. */
    @Column(name = "email_payments", nullable = false)
    @Builder.Default
    private boolean emailPayments = true;

    /** Ricevi notifica pagamenti e fatture via SMS. */
    @Column(name = "sms_payments", nullable = false)
    @Builder.Default
    private boolean smsPayments = false;

    // ========== Timestamps ==========

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
