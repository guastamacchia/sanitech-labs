package it.sanitech.scheduling.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.commons.security.JwtClaimExtractor;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.scheduling.repositories.SlotRepository;
import it.sanitech.scheduling.repositories.entities.Slot;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import it.sanitech.scheduling.services.mapper.SlotMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Service di dominio per la gestione degli {@link Slot}.
 */
@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slots;
    private final SlotMapper slotMapper;
    private final DeptGuard deptGuard;
    private final DomainEventPublisher events;

    /**
     * Crea uno slot singolo. Medici possono creare slot per se stessi,
     * altrimenti richiede autorizzazione sul reparto.
     */
    @Transactional
    public SlotDto createSlot(SlotCreateDto dto, Authentication auth) {
        boolean isOwnSlot = SecurityUtils.isDoctor(auth)
                && JwtClaimExtractor.doctorId(auth)
                        .map(id -> id.equals(dto.doctorId()))
                        .orElse(false);
        if (!isOwnSlot) {
            deptGuard.checkCanManage(dto.departmentCode(), auth);
        }

        if (dto.startAt().isAfter(dto.endAt()) || dto.startAt().equals(dto.endAt())) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_INVALID_TIME_RANGE);
        }

        Slot entity = slotMapper.fromCreateDto(dto);
        Slot saved = slots.save(entity);

        events.publish(
                "SLOT",
                String.valueOf(saved.getId()),
                "SLOT_CREATED",
                Map.of(
                        "slotId", saved.getId(),
                        "doctorId", saved.getDoctorId(),
                        "departmentCode", saved.getDepartmentCode(),
                        "mode", saved.getMode().name(),
                        "startAt", saved.getStartAt().toString(),
                        "endAt", saved.getEndAt().toString()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS
        );

        return slotMapper.toDto(saved);
    }

    /**
     * Bulk create di slot (es. import/campagne di pubblicazione).
     */
    @Transactional
    public List<SlotDto> createSlotsBulk(List<SlotCreateDto> dtos, Authentication auth) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_EMPTY_SLOT_LIST);
        }
        return dtos.stream().map(dto -> createSlot(dto, auth)).toList();
    }

    /**
     * Ricerca slot disponibili con filtri opzionali e paginazione/sorting sicuro.
     */
    @Bulkhead(name = "schedulingRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<SlotDto> searchAvailableSlots(
            Long doctorId,
            String departmentCode,
            VisitMode mode,
            Instant from,
            Instant to,
            int page,
            int size,
            String[] sort
    ) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.Sorting.ALLOWED_SLOT_SORT_FIELDS, "startAt");
        Pageable pageable = PageableUtils.pageRequest(page, size, AppConstants.MAX_PAGE_SIZE, safeSort);

        Specification<Slot> spec = hasStatus(SlotStatus.AVAILABLE)
                .and(optionalEq("doctorId", doctorId))
                .and(optionalEqIgnoreCase("departmentCode", departmentCode))
                .and(optionalEq("mode", mode))
                .and(optionalStartAtGte(from))
                .and(optionalStartAtLte(to));

        return slots.findAll(spec, pageable).map(slotMapper::toDto);
    }

    /**
     * Cancella uno slot (solo se non prenotato).
     */
    @Transactional
    public void cancelSlot(Long id, Authentication auth) {
        Slot slot = slots.findById(id).orElseThrow(() -> NotFoundException.of("Slot", id));
        deptGuard.checkCanManage(slot.getDepartmentCode(), auth);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_ALREADY_BOOKED);
        }
        slot.markCancelled();
        slots.save(slot);

        events.publish(
                "SLOT",
                String.valueOf(slot.getId()),
                "SLOT_CANCELLED",
                Map.of("slotId", slot.getId(), "occurredAt", Instant.now().toString()),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS
        );
    }

    // ---------- Specifications helpers ----------

    private static Specification<Slot> hasStatus(SlotStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Slot> optionalEq(String field, Object value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }

    private static Specification<Slot> optionalEqIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get(field)), value.toUpperCase());
        };
    }

    private static Specification<Slot> optionalStartAtGte(Instant from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("startAt"), from);
    }

    private static Specification<Slot> optionalStartAtLte(Instant to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("startAt"), to);
    }
}
