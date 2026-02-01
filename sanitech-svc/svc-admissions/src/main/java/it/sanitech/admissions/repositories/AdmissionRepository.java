package it.sanitech.admissions.repositories;

import it.sanitech.admissions.repositories.entities.Admission;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

/**
 * Repository JPA per {@link Admission}.
 */
public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    long countByDepartmentCodeIgnoreCaseAndStatus(String departmentCode, AdmissionStatus status);

    Page<Admission> findByPatientId(Long patientId, Pageable pageable);

    Page<Admission> findByPatientIdAndStatus(Long patientId, AdmissionStatus status, Pageable pageable);

    Page<Admission> findByDepartmentCodeIgnoreCase(String departmentCode, Pageable pageable);

    Page<Admission> findByDepartmentCodeIgnoreCaseAndStatus(String departmentCode, AdmissionStatus status, Pageable pageable);

    Page<Admission> findByStatus(AdmissionStatus status, Pageable pageable);


    @Query("select a from Admission a where upper(a.departmentCode) in :deptCodes")
    Page<Admission> findByDepartmentCodeIn(@Param("deptCodes") Collection<String> deptCodes, Pageable pageable);

    @Query("select a from Admission a where upper(a.departmentCode) in :deptCodes and a.status = :status")
    Page<Admission> findByDepartmentCodeInAndStatus(@Param("deptCodes") Collection<String> deptCodes,
                                                    @Param("status") AdmissionStatus status,
                                                    Pageable pageable);

}
