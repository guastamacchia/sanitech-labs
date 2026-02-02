package it.sanitech.payments.repositories.entities;

import it.sanitech.payments.utilities.AppConstants;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entità persistente che rappresenta un ordine di pagamento.
 *
 * <p>
 * Un ordine è associato a un appuntamento ({@code appointmentId}) e a un paziente ({@code patientId}).
 * </p>
 */
@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_email", length = 255)
    private String patientEmail;

    @Column(name = "patient_name", length = 255)
    private String patientName;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMethod method;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String provider = AppConstants.Payments.PROVIDER_MANUAL;

    @Column(name = "provider_reference", length = 128)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Column(length = 255)
    private String description;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
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

    public void markCaptured() {
        this.status = PaymentStatus.CAPTURED;
    }

    public void markFailed(String providerRef) {
        this.status = PaymentStatus.FAILED;
        if (providerRef != null && !providerRef.isBlank()) this.providerReference = providerRef;
    }

    public void markCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }
}
