package it.sanitech.directory.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.DepartmentAccessDeniedException;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.integrations.keycloak.KeycloakAdminClient;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.DoctorRepository;
import it.sanitech.directory.repositories.PatientRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.repositories.entities.UserStatus;
import it.sanitech.directory.repositories.spec.PatientSpecifications;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientPhoneUpdateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.services.events.KeycloakUserSyncEvent;
import it.sanitech.directory.services.mapper.PatientMapper;
import it.sanitech.directory.utilities.AppConstants;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service applicativo per la gestione dei pazienti.
 *
 * <p>
 * Gestisce creazione, aggiornamento, ricerca paginata e cancellazione dei pazienti,
 * applicando controlli di coerenza sui reparti, normalizzazione dei dati anagrafici e
 * pubblicazione di eventi Outbox per la sincronizzazione con altri servizi.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String PATIENT_ROLE = "ROLE_PATIENT";

    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    private final PatientMapper patientMapper;
    private final DomainEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeptGuard deptGuard;
    private final KeycloakAdminClient keycloakAdminClient;

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
                .phone(Objects.isNull(dto.phone()) ? null : dto.phone().trim())
                .fiscalCode(Objects.isNull(dto.fiscalCode()) ? null : dto.fiscalCode().trim().toUpperCase(Locale.ROOT))
                .birthDate(dto.birthDate())
                .address(Objects.isNull(dto.address()) ? null : dto.address().trim())
                .status(UserStatus.PENDING)
                .registeredAt(Instant.now())
                .departments(deptCodes.isEmpty() ? new HashSet<>() : resolveDepartments(deptCodes))
                .build();

        Patient saved = patientRepository.save(entity);

        applicationEventPublisher.publishEvent(new KeycloakUserSyncEvent(
                AppConstants.Outbox.AggregateType.PATIENT,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                false,  // Utente disabilitato fino all'attivazione da parte dell'admin
                PATIENT_ROLE,
                null
        ));

        // Invia email di attivazione
        publishActivationEmailEvent(saved, auth);

        return patientMapper.toDto(saved);
    }

    public PatientDto createPublic(PatientCreateDto dto) {
        PatientCreateDto sanitized = new PatientCreateDto(
                dto.firstName(),
                dto.lastName(),
                dto.email(),
                dto.phone(),
                dto.fiscalCode(),
                dto.birthDate(),
                dto.address(),
                Set.of()
        );
        return create(sanitized, null);
    }

    public PatientDto patch(Long id, PatientUpdateDto dto, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        String previousEmail = entity.getEmail();

        if (Objects.nonNull(dto.email()) && !dto.email().isBlank()) {
            String email = normalizeEmail(dto.email());
            if (!email.equalsIgnoreCase(entity.getEmail()) && patientRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Esiste già un paziente con email '" + email + "'.");
            }
        }

        patientMapper.updateEntity(dto, entity);

        entity.setFirstName(entity.getFirstName().trim());
        entity.setLastName(entity.getLastName().trim());
        entity.setEmail(normalizeEmail(entity.getEmail()));
        if (Objects.nonNull(entity.getPhone())) {
            entity.setPhone(entity.getPhone().trim());
        }
        if (Objects.nonNull(entity.getFiscalCode())) {
            entity.setFiscalCode(entity.getFiscalCode().trim().toUpperCase(Locale.ROOT));
        }
        if (Objects.nonNull(entity.getAddress())) {
            entity.setAddress(entity.getAddress().trim());
        }

        if (Objects.nonNull(dto.departmentCodes())) {
            Set<String> deptCodes = normalizeCodes(dto.departmentCodes());
            if (!deptCodes.isEmpty()) {
                deptGuard.checkCanManageAll(deptCodes, auth);
                entity.setDepartments(resolveDepartments(deptCodes));
            } else {
                entity.setDepartments(new HashSet<>());
            }
        }

        Patient saved = patientRepository.save(entity);

        applicationEventPublisher.publishEvent(new KeycloakUserSyncEvent(
                AppConstants.Outbox.AggregateType.PATIENT,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                true,
                null,
                previousEmail
        ));

        return patientMapper.toDto(saved);
    }

    public void delete(Long id, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        keycloakAdminClient.disableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.PATIENT,
                entity.getId()
        );
        patientRepository.delete(entity);
    }

    public PatientDto disableAccess(Long id, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        entity.setStatus(UserStatus.DISABLED);
        keycloakAdminClient.disableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.PATIENT,
                entity.getId()
        );
        Patient saved = patientRepository.save(entity);

        publishAccountStatusEmailEvent(saved, AppConstants.Outbox.EventType.ACCOUNT_DISABLED_EMAIL_REQUESTED, auth);

        return patientMapper.toDto(saved);
    }

    public PatientDto activate(Long id, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        entity.setStatus(UserStatus.ACTIVE);
        entity.setActivatedAt(Instant.now());
        keycloakAdminClient.enableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.PATIENT,
                entity.getId()
        );
        Patient saved = patientRepository.save(entity);

        publishAccountStatusEmailEvent(saved, AppConstants.Outbox.EventType.ACCOUNT_ENABLED_EMAIL_REQUESTED, auth);

        return patientMapper.toDto(saved);
    }

    public void resendActivation(Long id, Authentication auth) {
        Patient entity = patientRepository.findById(id).orElseThrow(() -> NotFoundException.of("Paziente", id));
        if (entity.getStatus() != UserStatus.PENDING) {
            throw new IllegalArgumentException("Il paziente non è in stato PENDING.");
        }
        publishActivationEmailEvent(entity, auth);
    }

    /**
     * Pubblica evento per invio email di attivazione account.
     */
    private void publishActivationEmailEvent(Patient entity, Authentication auth) {
        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.PATIENT,
                String.valueOf(entity.getId()),
                AppConstants.Outbox.EventType.ACTIVATION_EMAIL_REQUESTED,
                Map.of(
                        "recipientType", AppConstants.Outbox.AggregateType.PATIENT,
                        "recipientId", entity.getEmail(),
                        "email", entity.getEmail(),
                        "firstName", entity.getFirstName(),
                        "lastName", entity.getLastName()
                ),
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                auth
        );
    }

    /**
     * Pubblica evento per invio email di cambio stato account (attivazione/disattivazione).
     */
    private void publishAccountStatusEmailEvent(Patient entity, String eventType, Authentication auth) {
        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.PATIENT,
                String.valueOf(entity.getId()),
                eventType,
                Map.of(
                        "recipientType", AppConstants.Outbox.AggregateType.PATIENT,
                        "recipientId", entity.getEmail(),
                        "email", entity.getEmail(),
                        "firstName", entity.getFirstName(),
                        "lastName", entity.getLastName()
                ),
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                auth
        );
    }

    /**
     * Restituisce i dati del paziente identificato dalla propria email (utilizzata come username).
     */
    @Transactional(readOnly = true)
    public PatientDto getByEmail(String email) {
        Patient entity = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> NotFoundException.of("Paziente", email));
        return patientMapper.toDto(entity);
    }

    /**
     * Aggiorna il numero di telefono del paziente identificato dalla propria email.
     * L'email non può essere modificata perché corrisponde allo username del portale.
     */
    public PatientDto updatePhone(String email, PatientPhoneUpdateDto dto, Authentication auth) {
        Patient entity = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> NotFoundException.of("Paziente", email));

        String phone = dto.phone();
        entity.setPhone(Objects.isNull(phone) || phone.isBlank() ? null : phone.trim());

        Patient saved = patientRepository.save(entity);

        applicationEventPublisher.publishEvent(new KeycloakUserSyncEvent(
                AppConstants.Outbox.AggregateType.PATIENT,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                true,
                null,
                null
        ));

        return patientMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Bulkhead(name = "directoryRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<PatientDto> searchAdmin(String q, String departmentCode, String status, Long doctorId, int page, int size, String[] sort) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.PATIENT_ALLOWED, "id");
        Pageable pageable = PageableUtils.pageRequest(page, size, MAX_PAGE_SIZE, safeSort);

        UserStatus userStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Ignora filtro status non valido
            }
        }

        // Se doctorId è presente, filtra i pazienti per il reparto del medico
        String effectiveDepartmentCode = departmentCode;
        if (doctorId != null && effectiveDepartmentCode == null) {
            effectiveDepartmentCode = doctorRepository.findById(doctorId)
                    .map(doctor -> doctor.getDepartment().getCode())
                    .orElse(null);
        }

        Page<Patient> result = patientRepository.findAll(
                PatientSpecifications.search(q, effectiveDepartmentCode, userStatus),
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
                PatientSpecifications.search(q, null, null).and(PatientSpecifications.inDepartments(allowedDepts)),
                pageable
        );

        return result.map(patientMapper::toDto);
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
        if (Objects.isNull(email)) throw new IllegalArgumentException("Email obbligatoria.");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeCodes(Set<String> codes) {
        if (Objects.isNull(codes) || codes.isEmpty()) return Set.of();
        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    /**
     * Trova un paziente per nome e cognome (case-insensitive).
     * Utilizzato internamente per il lookup email nelle notifiche televisita.
     *
     * @param firstName nome del paziente
     * @param lastName cognome del paziente
     * @return Optional contenente il DTO del paziente se trovato
     */
    @Transactional(readOnly = true)
    public Optional<PatientDto> findByName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return Optional.empty();
        }
        return patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName.trim(), lastName.trim())
                .map(patientMapper::toDto);
    }
}
