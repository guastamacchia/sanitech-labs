package it.sanitech.consents.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Consenso privacy generico del paziente (GDPR, privacy policy, consenso terapia).
 * <p>
 * Questo tipo di consenso è diverso dal consenso medico-specifico ({@link Consent}):
 * rappresenta l'accettazione di termini generali da parte del paziente.
 * </p>
 */
@Entity
@Table(
        name = "privacy_consents",
        uniqueConstraints = @UniqueConstraint(name = "uk_privacy_consents_patient_type",
                columnNames = {"patient_id", "consent_type"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class PrivacyConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, updatable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 32, updatable = false)
    private PrivacyConsentType consentType;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.accepted && this.signedAt == null) {
            this.signedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Aggiorna lo stato di accettazione del consenso.
     *
     * @param accepted nuovo stato di accettazione
     */
    public void updateAcceptance(boolean accepted) {
        this.accepted = accepted;
        if (accepted) {
            this.signedAt = Instant.now();
        }
    }
}
