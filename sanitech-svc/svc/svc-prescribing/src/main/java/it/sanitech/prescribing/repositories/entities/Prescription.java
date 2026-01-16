package it.sanitech.prescribing.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescrizione medica associata a un paziente e redatta da un medico.
 *
 * <p>
 * Nota architetturale: i riferimenti a paziente/medico sono gestiti per ID (no FK cross-service).
 * </p>
 */
@Entity
@Table(name = "prescriptions",
        indexes = {
                @Index(name = "idx_prescriptions_patient", columnList = "patient_id"),
                @Index(name = "idx_prescriptions_doctor", columnList = "doctor_id"),
                @Index(name = "idx_prescriptions_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificativo applicativo del paziente.
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Identificativo applicativo del medico (autore della prescrizione).
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /**
     * Reparto contestuale (codice) in cui si colloca la prescrizione.
     */
    @Column(name = "department_code", nullable = false, length = 80)
    private String departmentCode;

    /**
     * Stato della prescrizione.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionStatus status;

    /**
     * Note cliniche (opzionali).
     */
    @Column(columnDefinition = "text")
    private String notes;

    /**
     * Timestamp di creazione.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Timestamp dell'ultimo aggiornamento.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Timestamp di emissione (quando la prescrizione diventa effettiva).
     */
    @Column(name = "issued_at")
    private Instant issuedAt;

    /**
     * Timestamp di annullamento.
     */
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    /**
     * Righe di prescrizione (farmaci/terapie).
     */
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = (createdAt == null) ? now : createdAt;
        updatedAt = (updatedAt == null) ? now : updatedAt;
        if (status == null) {
            status = PrescriptionStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Sostituisce le righe della prescrizione in modo sicuro per JPA (orphanRemoval).
     */
    public void replaceItems(List<PrescriptionItem> newItems) {
        items.clear();
        if (newItems == null) {
            return;
        }
        newItems.forEach(this::addItem);
    }

    /**
     * Aggiunge una riga, assicurando la back-reference per JPA.
     */
    public void addItem(PrescriptionItem item) {
        if (item == null) {
            return;
        }
        item.setPrescription(this);
        items.add(item);
    }

    public void markIssued() {
        status = PrescriptionStatus.ISSUED;
        issuedAt = Instant.now();
    }

    public void markCancelled() {
        status = PrescriptionStatus.CANCELLED;
        cancelledAt = Instant.now();
    }
}
