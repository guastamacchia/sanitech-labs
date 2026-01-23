package it.sanitech.admissions.repositories;

import it.sanitech.admissions.repositories.entities.DepartmentCapacity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository JPA per la capacità posti letto per reparto.
 */
public interface DepartmentCapacityRepository extends JpaRepository<DepartmentCapacity, String> {

    /**
     * Lock pessimista sulla riga del reparto per evitare race condition su ammissioni concorrenti.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from DepartmentCapacity c where upper(c.deptCode) = upper(:dept)")
    Optional<DepartmentCapacity> lockByDeptCode(@Param("dept") String dept);
}
