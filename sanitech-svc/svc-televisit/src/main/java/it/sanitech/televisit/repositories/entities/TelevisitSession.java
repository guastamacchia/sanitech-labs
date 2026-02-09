package it.sanitech.televisit.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Entità che rappresenta una sessione di video-visita (Televisit).
 *
 * <p>La sessione contiene:
 * <ul>
 *   <li>room LiveKit (nome univoco)</li>
 *   <li>partecipanti (identity/subject Keycloak di medico e paziente)</li>
 *   <li>reparto di competenza (usato per ABAC via claim {@code dept})</li>
 *   <li>stato e timestamp di scheduling/avvio/fine</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "televisit_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_televisit_room", columnNames = "room_name"),
        indexes = {
                @Index(name = "idx_televisit_dept_status", columnList = "department,status"),
                @Index(name = "idx_televisit_doctor", columnList = "doctor_subject"),
                @Index(name = "idx_televisit_patient", columnList = "patient_subject")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelevisitSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome room LiveKit (univoco).
     */
    @Column(name = "room_name", nullable = false, length = 128)
    private String roomName;

    /**
     * Reparto della sessione (codice), usato per ABAC (DEPT_*).
     */
    @Column(nullable = false, length = 80)
    private String department;

    /**
     * Identity/subject Keycloak del medico.
     */
    @Column(name = "doctor_subject", nullable = false, length = 128)
    private String doctorSubject;

    /**
     * Identity/subject Keycloak del paziente.
     */
    @Column(name = "patient_subject", nullable = false, length = 128)
    private String patientSubject;

    /**
     * Data/ora prevista (scheduling).
     */
    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TelevisitStatus status;

    /**
     * Note cliniche del medico sulla visita.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = TelevisitStatus.CREATED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void markActive() {
        this.status = TelevisitStatus.ACTIVE;
        this.startedAt = OffsetDateTime.now();
    }

    public void markEnded() {
        this.status = TelevisitStatus.ENDED;
        this.endedAt = OffsetDateTime.now();
    }

    public void markCanceled() {
        this.status = TelevisitStatus.CANCELED;
        this.endedAt = OffsetDateTime.now();
    }
}
