package it.sanitech.consents.repositories;

import it.sanitech.consents.domain.Consent;
import it.sanitech.consents.domain.ConsentScope;
import it.sanitech.consents.domain.ConsentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per {@link Consent}.
 */
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    Optional<Consent> findByPatientIdAndDoctorIdAndScope(Long patientId, Long doctorId, ConsentScope scope);

    boolean existsByPatientIdAndDoctorIdAndScopeAndStatus(Long patientId, Long doctorId, ConsentScope scope, ConsentStatus status);

    List<Consent> findByPatientIdOrderByUpdatedAtDesc(Long patientId);

    Page<Consent> findByDoctorIdOrderByUpdatedAtDesc(Long doctorId, Pageable pageable);
}
