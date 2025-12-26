package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository per l'anagrafica {@link Department}.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Department> findByCodeIgnoreCase(String code);

    List<Department> findByCodeIn(Collection<String> codes);
}
