package it.sanitech.directory.repositories;

import it.sanitech.directory.repositories.entities.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository per le preferenze di notifica dei pazienti.
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    /**
     * Trova le preferenze di notifica per un paziente specifico.
     *
     * @param patientId ID del paziente
     * @return Optional con le preferenze se esistono
     */
    Optional<NotificationPreference> findByPatientId(Long patientId);

    /**
     * Verifica se esistono preferenze per un paziente.
     *
     * @param patientId ID del paziente
     * @return true se esistono preferenze
     */
    boolean existsByPatientId(Long patientId);
}
