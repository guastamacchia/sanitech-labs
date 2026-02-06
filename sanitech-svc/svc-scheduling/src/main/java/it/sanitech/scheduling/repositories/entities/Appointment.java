package it.sanitech.scheduling.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Appuntamento prenotato su uno slot.
 *
 * <p>
 * Il servizio memorizza solo riferimenti (ID) a medico e paziente; i dettagli anagrafici
 * restano nel bounded context Directory. Questo riduce la duplicazione e limita la
 * superficie di dati sensibili nel servizio Scheduling.
 * </p>
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Slot prenotato (vincolo UNIQUE a livello DB: uno slot → al più un appuntamento). */
    @Column(name = "slot_id", unique = true)
    private Long slotId;

    /** Identificativo paziente (reference ID, gestito da Directory). */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** Identificativo medico (reference ID, gestito da Directory). */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /** Codice reparto (copiato dallo slot per semplificare query). */
    @Column(name = "department_code", nullable = false)
    private String departmentCode;

    /** Modalità prestazione (copiata dallo slot). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisitMode mode;

    /** Inizio (copiato dallo slot). */
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    /** Fine (copiata dallo slot). */
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    /** Stato dell'appuntamento. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status;

    /** Motivo della visita (opzionale). */
    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public void cancel(Instant when) {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = when;
    }

    public void complete(Instant when) {
        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = when;
    }
}
