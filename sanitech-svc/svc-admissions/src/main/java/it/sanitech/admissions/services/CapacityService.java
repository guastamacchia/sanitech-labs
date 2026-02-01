package it.sanitech.admissions.services;

import it.sanitech.admissions.repositories.AdmissionRepository;
import it.sanitech.admissions.repositories.DepartmentCapacityRepository;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.repositories.entities.DepartmentCapacity;
import it.sanitech.admissions.services.dto.CapacityDto;
import it.sanitech.commons.exception.ConflictException;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service applicativo per la gestione della capacità posti letto per reparto.
 */
@Service
@RequiredArgsConstructor
public class CapacityService {

    private static final String AGGREGATE_TYPE = "DEPARTMENT_CAPACITY";
    private static final String EVT_CAPACITY_SET = "CAPACITY_SET";

    private final DepartmentCapacityRepository capacityRepository;
    private final AdmissionRepository admissions;
    private final DomainEventPublisher domainEvents;

    @Transactional(readOnly = true)
    public List<CapacityDto> listAll() {
        return capacityRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CapacityDto get(String deptCode) {
        DepartmentCapacity cap = capacityRepository.findById(deptCode.toUpperCase())
                .orElseThrow(() -> new ConflictException("Capacità non configurata per il reparto: " + deptCode));
        return toDto(cap);
    }

    @Transactional
    public CapacityDto upsert(String deptCode, int totalBeds) {
        if (totalBeds < 0) {
            throw new IllegalArgumentException("totalBeds non può essere negativo");
        }

        String dept = deptCode.trim().toUpperCase();

        DepartmentCapacity cap = capacityRepository.findById(dept)
                .orElseGet(() -> new DepartmentCapacity(dept, 0, Instant.now()));

        cap.setTotalBeds(totalBeds);
        cap.setUpdatedAt(Instant.now());

        DepartmentCapacity saved = capacityRepository.save(cap);

        domainEvents.publish(
                AGGREGATE_TYPE,
                saved.getDeptCode(),
                EVT_CAPACITY_SET,
                Map.of(
                        "departmentCode", saved.getDeptCode(),
                        "totalBeds", saved.getTotalBeds(),
                        "updatedAt", saved.getUpdatedAt().toString()
                )
        );

        return toDto(saved);
    }

    private CapacityDto toDto(DepartmentCapacity cap) {
        long occupied = admissions.countByDepartmentCodeIgnoreCaseAndStatus(cap.getDeptCode(), AdmissionStatus.ACTIVE);
        long available = Math.max(0, cap.getTotalBeds() - occupied);

        return new CapacityDto(
                cap.getDeptCode(),
                cap.getTotalBeds(),
                occupied,
                available,
                cap.getUpdatedAt()
        );
    }
}
