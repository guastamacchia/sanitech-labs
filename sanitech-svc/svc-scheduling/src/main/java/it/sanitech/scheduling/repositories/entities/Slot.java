package it.sanitech.scheduling.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Slot di disponibilità pubblicato per un medico e un reparto.
 *
 * <p>
 * Lo slot rappresenta un "contenitore" prenotabile: quando viene prenotato,
 * lo stato passa a {@link SlotStatus#BOOKED} e viene creato un {@code Appointment}.
 * </p>
 */
@Entity
@Table(
        name = "slots",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_slots_doctor_start",
                columnNames = {"doctor_id", "start_at"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificativo del medico (reference ID, gestito dal bounded context Directory). */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /** Codice reparto (es. CARD, DERM). */
    @Column(name = "department_code", nullable = false)
    private String departmentCode;

    /** Modalità prestazione: in presenza o televisita. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisitMode mode;

    /** Inizio slot (istante UTC). */
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    /** Fine slot (istante UTC). */
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    /** Stato dello slot. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SlotStatus status;

    /** Timestamp creazione (generato DB, ma mantenuto anche lato entity per lettura). */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public void markBooked() {
        this.status = SlotStatus.BOOKED;
    }

    public void markCancelled() {
        this.status = SlotStatus.CANCELLED;
    }

    public void markAvailable() {
        this.status = SlotStatus.AVAILABLE;
    }
}
