package it.sanitech.directory.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.DomainEventPublisher;
import it.sanitech.directory.repositories.DoctorRepository;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.SpecializationRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.Specialization;
import it.sanitech.directory.repositories.spec.DoctorSpecifications;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import it.sanitech.directory.services.mapper.DoctorMapper;
import it.sanitech.commons.utilities.AppConstants;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service applicativo per la gestione dei medici.
 *
 * <p>
 * Coordina la logica di dominio per la creazione, l'aggiornamento, la ricerca paginata e la
 * cancellazione dei medici, includendo la risoluzione di reparti/specializzazioni, la
 * validazione di unicità dell'email e la pubblicazione di eventi Outbox per l'integrazione
 * asincrona con altri servizi.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final SpecializationRepository specializationRepository;

    private final DoctorMapper doctorMapper;
    private final DomainEventPublisher eventPublisher;
    private final DeptGuard deptGuard;

    @Transactional(readOnly = true)
    public DoctorDto get(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        return doctorMapper.toDto(entity);
    }

    public DoctorDto create(DoctorCreateDto dto, Authentication auth) {
        String email = normalizeEmail(dto.email());

        if (doctorRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Esiste già un medico con email '" + email + "'.");
        }

        Set<String> deptCodes = normalizeCodes(dto.departmentCodes());
        Set<String> specCodes = normalizeCodes(dto.specializationCodes());

        // ABAC: se l'utente non è admin, deve avere DEPT_* per tutti i reparti richiesti.
        deptGuard.checkCanManageAll(deptCodes, auth);

        Set<Department> departments = resolveDepartments(deptCodes);
        Set<Specialization> specializations = resolveSpecializations(specCodes);

        Doctor entity = Doctor.builder()
                .firstName(dto.firstName().trim())
                .lastName(dto.lastName().trim())
                .email(email)
                .departments(departments)
                .specializations(specializations)
                .build();

        Doctor saved = doctorRepository.save(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.DOCTOR_CREATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "departments", deptCodes,
                        "specializations", specCodes
                )
        );

        return doctorMapper.toDto(saved);
    }

    public DoctorDto patch(Long id, DoctorUpdateDto dto, Authentication auth) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));

        if (dto.email() != null && !dto.email().isBlank()) {
            String email = normalizeEmail(dto.email());
            if (!email.equalsIgnoreCase(entity.getEmail()) && doctorRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Esiste già un medico con email '" + email + "'.");
            }
        }

        // Aggiorna campi semplici (i null sono ignorati)
        doctorMapper.updateEntity(dto, entity);

        // Normalizzazioni post-mapping
        entity.setFirstName(entity.getFirstName().trim());
        entity.setLastName(entity.getLastName().trim());
        entity.setEmail(normalizeEmail(entity.getEmail()));

        // Reparti e specializzazioni: se presenti nel DTO, sostituiscono l'insieme corrente.
        if (dto.departmentCodes() != null) {
            Set<String> deptCodes = normalizeCodes(dto.departmentCodes());
            deptGuard.checkCanManageAll(deptCodes, auth);
            entity.setDepartments(resolveDepartments(deptCodes));
        }

        if (dto.specializationCodes() != null) {
            Set<String> specCodes = normalizeCodes(dto.specializationCodes());
            entity.setSpecializations(resolveSpecializations(specCodes));
        }

        Doctor saved = doctorRepository.save(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.DOCTOR_UPDATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "departments", saved.getDepartments().stream().map(Department::getCode).collect(Collectors.toSet()),
                        "specializations", saved.getSpecializations().stream().map(Specialization::getCode).collect(Collectors.toSet())
                )
        );

        return doctorMapper.toDto(saved);
    }

    public void delete(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        doctorRepository.delete(entity);

        eventPublisher.add(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(id),
                AppConstants.Outbox.EventType.DOCTOR_DELETED,
                Map.of("id", id)
        );
    }

    /**
     * Ricerca paginata con filtri combinabili.
     *
     * <p>
     * Annotata con {@link Bulkhead} per limitare le chiamate concorrenti verso il DB.
     * </p>
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "directoryRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<DoctorDto> search(String q, String departmentCode, String specializationCode,
                                  int page, int size, String[] sort) {

        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.DOCTOR_ALLOWED, "id");
        Pageable pageable = PageableUtils.pageRequest(page, size, MAX_PAGE_SIZE, safeSort);

        Page<Doctor> result = doctorRepository.findAll(
                DoctorSpecifications.search(q, departmentCode, specializationCode),
                pageable
        );

        return result.map(doctorMapper::toDto);
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

    private Set<Specialization> resolveSpecializations(Set<String> specCodes) {
        List<Specialization> found = specializationRepository.findByCodeIn(specCodes);
        Set<String> foundCodes = found.stream().map(Specialization::getCode).collect(Collectors.toSet());

        Set<String> missing = specCodes.stream()
                .filter(c -> !foundCodes.contains(c))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Specializzazioni non valide: " + String.join(",", missing));
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
