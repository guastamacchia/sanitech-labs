package it.sanitech.docs.repositories;

import it.sanitech.docs.repositories.entities.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

/**
 * Repository Spring Data JPA per l'entità {@link Document}.
 */
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Lista documenti di un paziente, paginata.
     */
    Page<Document> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Lista documenti di un paziente filtrando i reparti ammessi.
     *
     * <p>
     * Usato tipicamente per l'accesso del medico: si limita ai reparti presenti tra le sue authorities {@code DEPT_*}.
     * </p>
     */
    Page<Document> findByPatientIdAndDepartmentCodeIn(Long patientId, Collection<String> departmentCode, Pageable pageable);
}
