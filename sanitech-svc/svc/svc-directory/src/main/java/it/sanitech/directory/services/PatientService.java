package it.sanitech.directory.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.DepartmentAccessDeniedException;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.DomainEventPublisher;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.PatientRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.repositories.spec.PatientSpecifications;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.services.mapper.PatientMapper;
import it.sanitech.commons.utilities.AppConstants;
import it.sanitech.commons.utilities.CsvUtils;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service applicativo per la gestione dei Pazienti.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;

    private final PatientMapper patientMapper;
    private final DomainEventPublisher eventPublisher;
    private final DeptGuard deptGuard;

    @Transactional(readOnly = true)
    public PatientDto get(Long id) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        return patientMapper.toDto(entity);
    }

    /**
     * Lettura paziente in contesto DOCTOR: consente accesso solo se c'è intersezione
     * tra reparti del paziente e reparti presenti nelle authority {@code DEPT_*}.
     */
    @Transactional(readOnly = true)
    public PatientDto getForDoctor(Long id, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));

        Set<String> allowedDepts = deptGuard.extractDeptCodes(auth);
        boolean allowed = entity.getDepartments().stream()
                .map(Department::getCode)
                .anyMatch(allowedDepts::contains);

        if (!allowed) {
            throw DepartmentAccessDeniedException.forDepartments(
                    entity.getDepartments().stream().map(Department::getCode).collect(Collectors.toSet())
            );
        }
        return patientMapper.toDto(entity);
    }

    public PatientDto create(PatientCreateDto dto, Authentication auth) {
        String email = normalizeEmail(dto.email());

        if (patientRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Esiste già un paziente con email '" + email + "'.");
        }

        Set<String> deptCodes = normalizeCodes(dto.departmentCodes());
        if (!deptCodes.isEmpty()) {
            deptGuard.checkCanManageAll(deptCodes, auth);
        }

        Patient entity = Patient.builder()
                .firstName(dto.firstName().trim())
                .lastName(dto.lastName().trim())
                .email(email)
                .phone(dto.phone() == null ? null : dto.phone().trim())
                .departments(deptCodes.isEmpty() ? new HashSet<>() : resolveDepartments(deptCodes))
                .build();

        Patient saved = patientRepository.save(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.PATIENT,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.PATIENT_CREATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "phone", saved.getPhone(),
                        "departments", saved.getDepartments().stream().map(Department::getCode).collect(Collectors.toSet())
                )
        );

        return patientMapper.toDto(saved);
    }

    public PatientDto patch(Long id, PatientUpdateDto dto, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));

        if (dto.email() != null && !dto.email().isBlank()) {
            String email = normalizeEmail(dto.email());
            if (!email.equalsIgnoreCase(entity.getEmail()) && patientRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Esiste già un paziente con email '" + email + "'.");
            }
        }

        patientMapper.updateEntity(dto, entity);

        entity.setFirstName(entity.getFirstName().trim());
        entity.setLastName(entity.getLastName().trim());
        entity.setEmail(normalizeEmail(entity.getEmail()));
        if (entity.getPhone() != null) {
            entity.setPhone(entity.getPhone().trim());
        }

        if (dto.departmentCodes() != null) {
            Set<String> deptCodes = normalizeCodes(dto.departmentCodes());
            if (!deptCodes.isEmpty()) {
                deptGuard.checkCanManageAll(deptCodes, auth);
                entity.setDepartments(resolveDepartments(deptCodes));
            } else {
                entity.setDepartments(new HashSet<>());
            }
        }

        Patient saved = patientRepository.save(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.PATIENT,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.PATIENT_UPDATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "phone", saved.getPhone(),
                        "departments", saved.getDepartments().stream().map(Department::getCode).collect(Collectors.toSet())
                )
        );

        return patientMapper.toDto(saved);
    }

    public void delete(Long id) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        patientRepository.delete(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.PATIENT,
                String.valueOf(id),
                AppConstants.Outbox.EventType.PATIENT_DELETED,
                Map.of("id", id)
        );
    }

    @Transactional(readOnly = true)
    @Bulkhead(name = "directoryRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<PatientDto> searchAdmin(String q, String departmentCode, int page, int size, String[] sort) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.PATIENT_ALLOWED, "id");
        Pageable pageable = PageableUtils.pageRequest(page, size, MAX_PAGE_SIZE, safeSort);

        Page<Patient> result = patientRepository.findAll(
                PatientSpecifications.search(q, departmentCode),
                pageable
        );

        return result.map(patientMapper::toDto);
    }

    /**
     * Ricerca pazienti in contesto DOCTOR: applica filtro ABAC sui reparti dell'utente.
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "directoryRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<PatientDto> searchForDoctor(String q, int page, int size, String[] sort, Authentication auth) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.PATIENT_ALLOWED, "id");
        Pageable pageable = PageableUtils.pageRequest(page, size, MAX_PAGE_SIZE, safeSort);

        Set<String> allowedDepts = deptGuard.extractDeptCodes(auth);
        if (allowedDepts.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Patient> result = patientRepository.findAll(
                PatientSpecifications.search(q, null).and(PatientSpecifications.inDepartments(allowedDepts)),
                pageable
        );

        return result.map(patientMapper::toDto);
    }

    public List<PatientDto> bulkCreate(List<PatientCreateDto> items, Authentication auth) {
        if (items == null || items.isEmpty()) return List.of();
        List<PatientDto> created = new ArrayList<>(items.size());
        for (PatientCreateDto dto : items) {
            created.add(create(dto, auth));
        }
        return created;
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(String q, String departmentCode) {
        List<Patient> patients = patientRepository.findAll(
                PatientSpecifications.search(q, departmentCode),
                Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())
        );

        StringBuilder sb = new StringBuilder();
        sb.append("id,firstName,lastName,email,phone,departments\n");

        for (Patient p : patients) {
            String departments = CsvUtils.join(p.getDepartments().stream().map(Department::getCode).toList(), "|");

            sb.append(p.getId()).append(',')
                    .append(CsvUtils.csv(p.getFirstName())).append(',')
                    .append(CsvUtils.csv(p.getLastName())).append(',')
                    .append(CsvUtils.csv(p.getEmail())).append(',')
                    .append(CsvUtils.csv(p.getPhone())).append(',')
                    .append(CsvUtils.csv(departments))
                    .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Set<Department> resolveDepartments(Set<String> deptCodes) {
        List<Department> found = departmentRepository.findByCodeIn(deptCodes);
        Set<String> foundCodes = found.stream().map(Department::getCode).collect(Collectors.toSet());

        Set<String> missing = deptCodes.stream()
                .filter(c -> !foundCodes.contains(c))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Reparti non validi: " + String.join(",", missing));
        }
        return new HashSet<>(found);
    }

    private String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email obbligatoria.");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return Set.of();
        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
