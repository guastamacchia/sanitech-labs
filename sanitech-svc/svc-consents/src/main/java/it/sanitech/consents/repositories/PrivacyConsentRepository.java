package it.sanitech.consents.repositories;

import it.sanitech.consents.repositories.entities.PrivacyConsent;
import it.sanitech.consents.repositories.entities.PrivacyConsentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per i consensi privacy.
 */
public interface PrivacyConsentRepository extends JpaRepository<PrivacyConsent, Long> {

    Optional<PrivacyConsent> findByPatientIdAndConsentType(Long patientId, PrivacyConsentType consentType);

    List<PrivacyConsent> findByPatientIdOrderByUpdatedAtDesc(Long patientId);
}
