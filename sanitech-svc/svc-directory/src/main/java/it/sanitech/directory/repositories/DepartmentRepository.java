package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per l'anagrafica {@link Department}.
 *
 * <p>
 * Espone metodi di query focalizzati sui codici reparto e sulle ricerche testuali
 * (codice/nome), utilizzati dal service layer per validazioni di unicità e per
 * filtri di ricerca lato API.
 * </p>
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Department> findByCodeIgnoreCase(String code);

    List<Department> findByCodeIn(Collection<String> codes);

    List<Department> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(String code, String name);
}
