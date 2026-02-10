package it.sanitech.prescribing.repositories;

import it.sanitech.prescribing.repositories.entities.Prescription;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository Spring Data JPA per l'entità {@link Prescription}.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Prescription> findByIdAndPatientId(Long id, Long patientId);

    @EntityGraph(attributePaths = "items")
    Optional<Prescription> findByIdAndDoctorId(Long id, Long doctorId);

    @EntityGraph(attributePaths = "items")
    Optional<Prescription> findDetailedById(Long id);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByPatientIdAndStatusIn(Long patientId, Collection<PrescriptionStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByDoctorId(Long doctorId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByPatientIdAndDepartmentCodeIgnoreCase(Long patientId, String departmentCode, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    @Query("select p from Prescription p")
    Page<Prescription> findAllWithItems(Pageable pageable);
}

