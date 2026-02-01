package it.sanitech.scheduling.repositories;

import it.sanitech.scheduling.repositories.entities.Slot;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository Spring Data JPA per l'entità {@link Slot}.
 */
public interface SlotRepository extends JpaRepository<Slot, Long>, JpaSpecificationExecutor<Slot> {

    /**
     * Carica lo slot in lock pessimistica per evitare doppie prenotazioni concorrenti.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(SlotStatus status);
}
