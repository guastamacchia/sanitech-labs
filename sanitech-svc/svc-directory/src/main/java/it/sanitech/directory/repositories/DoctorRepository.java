package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository Spring Data JPA per l'entità {@link Doctor}.
 *
 * <p>
 * Espone CRUD e supporto a query dinamiche tramite {@link JpaSpecificationExecutor}
 * (utilizzato per filtri combinabili su q/reparto/specializzazione).
 * </p>
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    /**
     * Verifica esistenza di un medico per email, ignorando maiuscole/minuscole.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Conta i medici associati a un reparto.
     *
     * @param departmentId identificatore del reparto
     * @return numero di medici associati
     */
    long countByDepartmentId(Long departmentId);

    /**
     * Conta i medici per ogni reparto in un'unica query.
     * Restituisce una lista di array [departmentId, count].
     *
     * @param departmentIds lista di identificatori di reparto
     * @return lista di proiezioni [departmentId, count]
     */
    @Query("SELECT d.department.id, COUNT(d) FROM Doctor d WHERE d.department.id IN :departmentIds GROUP BY d.department.id")
    List<Object[]> countByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);

    java.util.Optional<Doctor> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
