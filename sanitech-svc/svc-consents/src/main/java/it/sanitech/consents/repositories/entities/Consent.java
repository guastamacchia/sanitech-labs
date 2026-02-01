package it.sanitech.consents.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Consenso del paziente verso un medico per un determinato {@link ConsentScope}.
 * <p>
 * Questo microservizio NON applica il consenso durante la consultazione dell'elenco medici:
 * la verifica viene eseguita nei servizi clinici quando un medico accede ai dati del paziente.
 * </p>
 */
@Entity
@Table(
        name = "consents",
        uniqueConstraints = @UniqueConstraint(name = "uk_consents_patient_doctor_scope",
                columnNames = {"patient_id", "doctor_id", "scope"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, updatable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false, updatable = false)
    private Long doctorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 64, updatable = false)
    private ConsentScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ConsentStatus status;

    @Column(name = "granted_at")
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ConsentStatus.GRANTED;
        }
        if (this.status == ConsentStatus.GRANTED && this.grantedAt == null) {
            this.grantedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Applica la concessione del consenso (idempotente).
     *
     * @param expiresAt eventuale scadenza del consenso
     */
    public void grant(Instant expiresAt) {
        this.status = ConsentStatus.GRANTED;
        this.grantedAt = Instant.now();
        this.revokedAt = null;
        this.expiresAt = expiresAt;
    }

    /**
     * Revoca il consenso (idempotente).
     */
    public void revoke() {
        this.status = ConsentStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    /**
     * @return {@code true} se il consenso è concesso e non scaduto.
     */
    public boolean isCurrentlyGranted() {
        if (this.status != ConsentStatus.GRANTED) {
            return false;
        }
        return this.expiresAt == null || this.expiresAt.isAfter(Instant.now());
    }
}
