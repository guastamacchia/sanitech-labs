package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository per l'anagrafica {@link Specialization}.
 */
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Specialization> findByCodeIgnoreCase(String code);

    List<Specialization> findByCodeIn(Collection<String> codes);
}
