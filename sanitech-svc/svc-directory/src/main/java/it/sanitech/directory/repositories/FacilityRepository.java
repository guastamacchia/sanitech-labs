package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per l'anagrafica {@link Facility}.
 *
 * <p>
 * Supporta lookup per codice, insiemi di codici e ricerche testuali su codice/nome,
 * utili per validazioni e filtri applicativi nei servizi.
 * </p>
 */
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Facility> findByCodeIgnoreCase(String code);

    List<Facility> findByCodeIn(Collection<String> codes);

    List<Facility> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(String code, String name);
}
