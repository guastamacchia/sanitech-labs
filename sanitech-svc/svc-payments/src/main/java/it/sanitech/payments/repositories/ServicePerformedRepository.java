package it.sanitech.payments.repositories;

import it.sanitech.payments.repositories.entities.ServicePerformed;
import it.sanitech.payments.repositories.entities.ServicePerformedStatus;
import it.sanitech.payments.repositories.entities.ServiceSourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository per le prestazioni sanitarie.
 */
@Repository
public interface ServicePerformedRepository extends JpaRepository<ServicePerformed, Long>, JpaSpecificationExecutor<ServicePerformed> {

    /**
     * Verifica se esiste già una prestazione per la sorgente specificata.
     */
    boolean existsBySourceTypeAndSourceId(ServiceSourceType sourceType, Long sourceId);

    /**
     * Trova una prestazione dalla sorgente.
     */
    Optional<ServicePerformed> findBySourceTypeAndSourceId(ServiceSourceType sourceType, Long sourceId);

    /**
     * Trova prestazioni per paziente.
     */
    Page<ServicePerformed> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Trova prestazioni per paziente e stato.
     */
    Page<ServicePerformed> findByPatientIdAndStatus(Long patientId, ServicePerformedStatus status, Pageable pageable);

    /**
     * Trova prestazioni per stato.
     */
    Page<ServicePerformed> findByStatus(ServicePerformedStatus status, Pageable pageable);

    /**
     * Trova prestazioni in attesa di pagamento da più di N giorni.
     */
    @Query("SELECT s FROM ServicePerformed s WHERE s.status = :status AND s.performedAt < :before")
    List<ServicePerformed> findPendingOlderThan(
            @Param("status") ServicePerformedStatus status,
            @Param("before") Instant before);

    /**
     * Conta prestazioni per stato.
     */
    long countByStatus(ServicePerformedStatus status);

    /**
     * Conta prestazioni per stato e data esecuzione dopo.
     */
    long countByStatusAndPerformedAtAfter(ServicePerformedStatus status, Instant after);

    /**
     * Conta prestazioni con solleciti e pagate nel periodo.
     */
    @Query("SELECT COUNT(s) FROM ServicePerformed s WHERE s.status = :status AND s.reminderCount > 0 AND s.performedAt >= :after")
    long countPaidWithReminderAfter(@Param("status") ServicePerformedStatus status, @Param("after") Instant after);

    /**
     * Trova prestazioni per IDs.
     */
    List<ServicePerformed> findByIdIn(List<Long> ids);
}
