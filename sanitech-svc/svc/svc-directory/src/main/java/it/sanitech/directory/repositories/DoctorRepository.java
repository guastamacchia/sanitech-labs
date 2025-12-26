package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
