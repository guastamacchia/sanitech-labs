package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.Set;

/**
 * Repository Spring Data JPA per l'entità {@link Patient}.
 *
 * <p>
 * Oltre alle funzionalità CRUD e alle {@link JpaSpecificationExecutor} per filtri combinabili,
 * espone query dedicate al controllo ABAC sui reparti, utilizzate per limitare la visibilità
 * dei pazienti in contesto medico.
 * </p>
 */
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Restituisce una pagina di pazienti associati ad almeno uno dei reparti indicati.
     *
     * <p>
     * Utile per applicare policy ABAC: un DOCTOR vede solo pazienti del/dei propri reparti.
     * </p>
     */
    Page<Patient> findDistinctByDepartments_CodeIn(Set<String> departmentCodes, Pageable pageable);

    /**
     * Lookup paziente vincolata ai reparti consentiti (ABAC).
     */
    Optional<Patient> findByIdAndDepartments_CodeIn(Long id, Set<String> departmentCodes);

    /**
     * Restituisce il paziente con l'email indicata (case-insensitive).
     */
    Optional<Patient> findByEmailIgnoreCase(String email);
}
