package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per l'anagrafica {@link Specialization}.
 *
 * <p>
 * Supporta lookup per codice, insiemi di codici e ricerche testuali su codice/nome,
 * utili per validazioni e filtri applicativi nei servizi.
 * </p>
 */
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Specialization> findByCodeIgnoreCase(String code);

    List<Specialization> findByCodeIn(Collection<String> codes);

    List<Specialization> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(String code, String name);
}
