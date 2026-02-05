package it.sanitech.payments.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entità che rappresenta una prestazione sanitaria erogata (visita medica o ricovero).
 *
 * <p>
 * Le prestazioni vengono registrate automaticamente:
 * <ul>
 *   <li>Per le visite mediche: quando il medico conferma la conclusione della televisita</li>
 *   <li>Per i ricoveri: quando il paziente viene dimesso</li>
 * </ul>
 * </p>
 *
 * <p>
 * Importi di default:
 * <ul>
 *   <li>Visita medica: 100 EUR</li>
 *   <li>Ricovero: 20 EUR al giorno</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "services_performed",
        indexes = {
                @Index(name = "idx_sp_patient_created", columnList = "patient_id,created_at DESC"),
                @Index(name = "idx_sp_status", columnList = "status"),
                @Index(name = "idx_sp_source", columnList = "source_type,source_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePerformed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo di prestazione.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 32)
    private ServiceType serviceType;

    /**
     * Tipo di pagamento (VISITA, RICOVERO, ALTRO).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 32)
    private PaymentType paymentType;

    /**
     * Tipo di sorgente che ha generato la prestazione.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private ServiceSourceType sourceType;

    /**
     * ID dell'entità sorgente (televisita o ricovero).
     */
    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    /**
     * ID del paziente.
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /**
     * Subject Keycloak del paziente (per lookup).
     */
    @Column(name = "patient_subject", length = 128)
    private String patientSubject;

    /**
     * Nome del paziente (denormalizzato per UI).
     */
    @Column(name = "patient_name", length = 255)
    private String patientName;

    /**
     * Email del paziente (per solleciti).
     */
    @Column(name = "patient_email", length = 255)
    private String patientEmail;

    /**
     * ID del medico che ha erogato la prestazione.
     */
    @Column(name = "doctor_id")
    private Long doctorId;

    /**
     * Nome del medico (denormalizzato per UI).
     */
    @Column(name = "doctor_name", length = 255)
    private String doctorName;

    /**
     * Codice reparto.
     */
    @Column(name = "department_code", length = 80)
    private String departmentCode;

    /**
     * Descrizione della prestazione.
     */
    @Column(length = 500)
    private String description;

    /**
     * Importo in centesimi.
     */
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    /**
     * Valuta (default EUR).
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EUR";

    /**
     * Stato della prestazione/pagamento.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ServicePerformedStatus status;

    /**
     * Data di erogazione della prestazione (fine visita o dimissione).
     */
    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    /**
     * Data inizio (per ricoveri: data ammissione).
     */
    @Column(name = "started_at")
    private Instant startedAt;

    /**
     * Numero di giorni di ricovero (calcolato per ricoveri).
     */
    @Column(name = "days_count")
    private Integer daysCount;

    /**
     * Numero di solleciti inviati.
     */
    @Column(name = "reminder_count", nullable = false)
    @Builder.Default
    private int reminderCount = 0;

    /**
     * Data ultimo sollecito.
     */
    @Column(name = "last_reminder_at")
    private Instant lastReminderAt;

    /**
     * Note aggiuntive (es. motivo gratuità).
     */
    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 128)
    private String createdBy;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Marca la prestazione come pagata.
     */
    public void markPaid() {
        this.status = ServicePerformedStatus.PAID;
    }

    /**
     * Marca la prestazione come gratuita.
     */
    public void markFree(String reason) {
        this.status = ServicePerformedStatus.FREE;
        this.amountCents = 0;
        this.notes = reason;
    }

    /**
     * Annulla la prestazione.
     */
    public void markCancelled() {
        this.status = ServicePerformedStatus.CANCELLED;
    }

    /**
     * Incrementa il contatore solleciti.
     */
    public void recordReminderSent() {
        this.reminderCount++;
        this.lastReminderAt = Instant.now();
    }
}
