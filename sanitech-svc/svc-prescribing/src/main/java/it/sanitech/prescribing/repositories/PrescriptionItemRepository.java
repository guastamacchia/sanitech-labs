package it.sanitech.prescribing.repositories;

import it.sanitech.prescribing.repositories.entities.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository Spring Data JPA per {@link PrescriptionItem}.
 */
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    List<PrescriptionItem> findByPrescriptionIdOrderBySortOrderAscIdAsc(Long prescriptionId);
}
