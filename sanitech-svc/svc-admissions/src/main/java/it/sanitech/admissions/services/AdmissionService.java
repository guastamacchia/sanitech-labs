package it.sanitech.admissions.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.admissions.exception.NoBedAvailableException;
import it.sanitech.admissions.repositories.AdmissionRepository;
import it.sanitech.admissions.repositories.DepartmentCapacityRepository;
import it.sanitech.admissions.repositories.entities.*;
import it.sanitech.admissions.services.dto.AdmissionDto;
import it.sanitech.admissions.services.dto.create.AdmissionCreateDto;
import it.sanitech.admissions.services.dto.update.AdmissionUpdateDto;
import it.sanitech.admissions.services.mapper.AdmissionMapper;
import it.sanitech.commons.exception.ConflictException;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.commons.security.JwtClaimExtractor;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.commons.utilities.AppConstants;
import it.sanitech.admissions.utilities.AppConstants.Outbox;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service applicativo per la gestione dei ricoveri.
 */
@Service
@RequiredArgsConstructor
public class AdmissionService {

    private static final String AGGREGATE_TYPE = "ADMISSION";
    private static final String EVT_CREATED = "ADMISSION_CREATED";
    private static final String EVT_DISCHARGED = "ADMISSION_DISCHARGED";
    private static final String EVT_UPDATED = "ADMISSION_UPDATED";

    private final AdmissionRepository admissions;
    private final DepartmentCapacityRepository capacityRepository;
    private final AdmissionMapper mapper;
    private final DeptGuard deptGuard;
    private final DomainEventPublisher domainEvents;

    /**
     * Crea un nuovo ricovero verificando la disponibilità posti letto del reparto.
     */
    @Transactional
    public AdmissionDto admit(AdmissionCreateDto dto, Authentication auth) {
        String dept = normalizeDept(dto.departmentCode());
        deptGuard.checkCanManage(dept, auth);

        var capacity = capacityRepository.lockByDeptCode(dept)
                .orElseThrow(() -> new ConflictException("Capacità posti letto non configurata per il reparto: " + dept));

        long occupied = admissions.countByDepartmentCodeIgnoreCaseAndStatus(dept, AdmissionStatus.ACTIVE);
        if (occupied >= capacity.getTotalBeds()) {
            throw NoBedAvailableException.forDepartment(dept);
        }

        Admission admission = mapper.toEntity(dto);
        admission.setDepartmentCode(dept);
        admission.setStatus(AdmissionStatus.ACTIVE);
        admission.setAdmittedAt(Instant.now());

        Admission saved = admissions.save(admission);

        domainEvents.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                EVT_CREATED,
                Map.of(
                        "admissionId", saved.getId(),
                        "patientId", saved.getPatientId(),
                        "departmentCode", saved.getDepartmentCode(),
                        "admissionType", saved.getAdmissionType().name(),
                        "admittedAt", saved.getAdmittedAt().toString()
                ),
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Dimette un ricovero attivo.
     */
    @Transactional
    public AdmissionDto discharge(Long admissionId, Authentication auth) {
        Admission admission = admissions.findById(admissionId)
                .orElseThrow(() -> NotFoundException.of("Ricovero", admissionId));

        deptGuard.checkCanManage(admission.getDepartmentCode(), auth);

        if (admission.getStatus() != AdmissionStatus.ACTIVE) {
            throw new ConflictException("Ricovero non dimettibile: stato attuale=" + admission.getStatus());
        }

        admission.setStatus(AdmissionStatus.DISCHARGED);
        admission.setDischargedAt(Instant.now());

        Admission saved = admissions.save(admission);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("admissionId", saved.getId());
        eventPayload.put("patientId", saved.getPatientId());
        eventPayload.put("departmentCode", saved.getDepartmentCode());
        eventPayload.put("admittedAt", saved.getAdmittedAt().toString());
        eventPayload.put("dischargedAt", saved.getDischargedAt().toString());
        if (saved.getAttendingDoctorId() != null) {
            eventPayload.put("attendingDoctorId", saved.getAttendingDoctorId());
        }

        domainEvents.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                EVT_DISCHARGED,
                eventPayload,
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Aggiorna parzialmente un ricovero attivo (referente e/o note).
     */
    @Transactional
    public AdmissionDto update(Long admissionId, AdmissionUpdateDto dto, Authentication auth) {
        Admission admission = admissions.findById(admissionId)
                .orElseThrow(() -> NotFoundException.of("Ricovero", admissionId));

        deptGuard.checkCanManage(admission.getDepartmentCode(), auth);

        if (admission.getStatus() != AdmissionStatus.ACTIVE) {
            throw new ConflictException("Impossibile modificare un ricovero non attivo: stato=" + admission.getStatus());
        }

        boolean changed = false;

        if (dto.attendingDoctorId() != null) {
            admission.setAttendingDoctorId(dto.attendingDoctorId());
            changed = true;
        }

        if (dto.notes() != null) {
            admission.setNotes(dto.notes());
            changed = true;
        }

        if (!changed) {
            return mapper.toDto(admission);
        }

        Admission saved = admissions.save(admission);

        domainEvents.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                EVT_UPDATED,
                Map.of(
                        "admissionId", saved.getId(),
                        "patientId", saved.getPatientId(),
                        "departmentCode", saved.getDepartmentCode(),
                        "attendingDoctorId", saved.getAttendingDoctorId() != null ? saved.getAttendingDoctorId() : "",
                        "notes", saved.getNotes() != null ? saved.getNotes() : ""
                ),
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Ricerca/lista ricoveri.
     *
     * <p>
     * Regole:
     * <ul>
     *   <li>ADMIN: vede tutto (filtri opzionali).</li>
     *   <li>Non-ADMIN: vede solo i ricoveri dei reparti autorizzati (DEPT_*).</li>
     * </ul>
     * </p>
     */
    @Bulkhead(name = "admissionsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<AdmissionDto> list(Authentication auth, String department, AdmissionStatus status, Pageable pageable) {
        boolean isAdmin = SecurityUtils.hasAuthority(auth, AppConstants.Security.ROLE_ADMIN);

        if (isAdmin) {
            return listForAdmin(department, status, pageable).map(mapper::toDto);
        }

        Set<String> allowedDepts = SecurityUtils.departmentCodes(auth);
        if (allowedDepts.isEmpty()) {
            return Page.empty(pageable);
        }

        if (department != null && !department.isBlank()) {
            String dept = normalizeDept(department);
            deptGuard.checkCanManage(dept, auth);
            return listForAdmin(dept, status, pageable).map(mapper::toDto);
        }

        return (status == null
                ? admissions.findByDepartmentCodeIn(allowedDepts, pageable)
                : admissions.findByDepartmentCodeInAndStatus(allowedDepts, status, pageable)
        ).map(mapper::toDto);
    }

    /**
     * Lista i ricoveri del paziente autenticato.
     *
     * @param status   filtro opzionale sullo stato del ricovero
     * @param pageable parametri di paginazione
     * @param auth     oggetto Authentication contenente il JWT con claim {@code pid}
     * @return pagina di ricoveri del paziente
     */
    @Bulkhead(name = "admissionsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<AdmissionDto> listMine(AdmissionStatus status, Pageable pageable, Authentication auth) {
        Long patientId = JwtClaimExtractor.requirePatientId(auth);
        Page<Admission> page = (status == null)
                ? admissions.findByPatientId(patientId, pageable)
                : admissions.findByPatientIdAndStatus(patientId, status, pageable);
        return page.map(mapper::toDto);
    }

    private Page<Admission> listForAdmin(String dept, AdmissionStatus status, Pageable pageable) {
        if (dept == null || dept.isBlank()) {
            return (status == null) ? admissions.findAll(pageable) : admissions.findByStatus(status, pageable);
        }

        return (status == null)
                ? admissions.findByDepartmentCodeIgnoreCase(dept, pageable)
                : admissions.findByDepartmentCodeIgnoreCaseAndStatus(dept, status, pageable);
    }

    private static String normalizeDept(String dept) {
        if (dept == null) return null;
        return dept.trim().toUpperCase();
    }
}
