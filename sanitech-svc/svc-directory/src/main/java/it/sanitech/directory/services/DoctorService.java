package it.sanitech.directory.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.repositories.DoctorRepository;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.repositories.entities.UserStatus;
import it.sanitech.directory.repositories.spec.DoctorSpecifications;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.integrations.keycloak.KeycloakAdminClient;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import it.sanitech.directory.services.events.KeycloakUserSyncEvent;
import it.sanitech.directory.services.mapper.DoctorMapper;
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

/**
 * Service applicativo per la gestione dei medici.
 *
 * <p>
 * Coordina la logica di dominio per la creazione, l'aggiornamento, la ricerca paginata e la
 * cancellazione dei medici, includendo la risoluzione del reparto di appartenenza, la
 * validazione di unicità dell'email e la pubblicazione di eventi Outbox per l'integrazione
 * asincrona con altri servizi.
 * Gerarchia: Struttura -> Reparto -> Medico.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String DOCTOR_ROLE = "ROLE_DOCTOR";

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    private final DoctorMapper doctorMapper;
    private final DomainEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeptGuard deptGuard;
    private final KeycloakAdminClient keycloakAdminClient;

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

        String deptCode = normalizeCode(dto.departmentCode(), "Reparto obbligatorio.");

        // ABAC: se l'utente non è admin, deve avere DEPT_* per tutti i reparti richiesti.
        deptGuard.checkCanManageAll(Set.of(deptCode), auth);

        Department department = resolveDepartment(deptCode);

        Doctor entity = Doctor.builder()
                .firstName(dto.firstName().trim())
                .lastName(dto.lastName().trim())
                .email(email)
                .phone(normalizePhone(dto.phone()))
                .specialization(dto.specialization() != null ? dto.specialization().trim() : null)
                .department(department)
                .status(UserStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Doctor saved = doctorRepository.save(entity);

        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.DOCTOR_CREATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "departmentCode", deptCode,
                        "facilityCode", department.getFacility().getCode()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS
        );

        applicationEventPublisher.publishEvent(new KeycloakUserSyncEvent(
                AppConstants.Outbox.AggregateType.DOCTOR,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                false,  // Utente disabilitato fino all'attivazione da parte dell'admin
                DOCTOR_ROLE,
                null
        ));

        // Invia email di attivazione
        publishActivationEmailEvent(saved);

        return doctorMapper.toDto(saved);
    }

    public DoctorDto patch(Long id, DoctorUpdateDto dto, Authentication auth) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        String previousEmail = entity.getEmail();

        if (Objects.nonNull(dto.email()) && !dto.email().isBlank()) {
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
        entity.setPhone(normalizePhone(entity.getPhone()));

        // Reparto: se presente nel DTO, sostituisce il valore corrente.
        if (Objects.nonNull(dto.departmentCode())) {
            String deptCode = normalizeCode(dto.departmentCode(), "Reparto non valido.");
            deptGuard.checkCanManageAll(Set.of(deptCode), auth);
            entity.setDepartment(resolveDepartment(deptCode));
        }

        Doctor saved = doctorRepository.save(entity);

        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EventType.DOCTOR_UPDATED,
                Map.of(
                        "id", saved.getId(),
                        "firstName", saved.getFirstName(),
                        "lastName", saved.getLastName(),
                        "email", saved.getEmail(),
                        "departmentCode", saved.getDepartment().getCode(),
                        "facilityCode", saved.getDepartment().getFacility().getCode()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS
        );

        applicationEventPublisher.publishEvent(new KeycloakUserSyncEvent(
                AppConstants.Outbox.AggregateType.DOCTOR,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                true,
                null,
                previousEmail
        ));

        return doctorMapper.toDto(saved);
    }

    public void delete(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        keycloakAdminClient.disableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.DOCTOR,
                entity.getId()
        );
        doctorRepository.delete(entity);

        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(id),
                AppConstants.Outbox.EventType.DOCTOR_DELETED,
                Map.of("id", id),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS
        );
    }

    public DoctorDto disableAccess(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        entity.setStatus(UserStatus.DISABLED);
        keycloakAdminClient.disableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.DOCTOR,
                entity.getId()
        );
        Doctor saved = doctorRepository.save(entity);

        publishAccountStatusEmailEvent(saved, AppConstants.Outbox.EventType.ACCOUNT_DISABLED_EMAIL_REQUESTED);

        return doctorMapper.toDto(saved);
    }

    public DoctorDto activate(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        entity.setStatus(UserStatus.ACTIVE);
        entity.setActivatedAt(Instant.now());
        keycloakAdminClient.enableUser(
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                AppConstants.Outbox.AggregateType.DOCTOR,
                entity.getId()
        );
        Doctor saved = doctorRepository.save(entity);

        publishAccountStatusEmailEvent(saved, AppConstants.Outbox.EventType.ACCOUNT_ENABLED_EMAIL_REQUESTED);

        return doctorMapper.toDto(saved);
    }

    public void resendActivation(Long id) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        if (entity.getStatus() != UserStatus.PENDING) {
            throw new IllegalArgumentException("Il medico non è in stato PENDING.");
        }
        publishActivationEmailEvent(entity);
    }

    /**
     * Pubblica evento per invio email di attivazione account.
     */
    private void publishActivationEmailEvent(Doctor entity) {
        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(entity.getId()),
                AppConstants.Outbox.EventType.ACTIVATION_EMAIL_REQUESTED,
                Map.of(
                        "recipientType", AppConstants.Outbox.AggregateType.DOCTOR,
                        "recipientId", String.valueOf(entity.getId()),
                        "email", entity.getEmail(),
                        "firstName", entity.getFirstName(),
                        "lastName", entity.getLastName()
                ),
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS
        );
    }

    /**
     * Pubblica evento per invio email di cambio stato account (attivazione/disattivazione).
     */
    private void publishAccountStatusEmailEvent(Doctor entity, String eventType) {
        eventPublisher.publish(
                AppConstants.Outbox.AggregateType.DOCTOR,
                String.valueOf(entity.getId()),
                eventType,
                Map.of(
                        "recipientType", AppConstants.Outbox.AggregateType.DOCTOR,
                        "recipientId", String.valueOf(entity.getId()),
                        "email", entity.getEmail(),
                        "firstName", entity.getFirstName(),
                        "lastName", entity.getLastName()
                ),
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS
        );
    }

    public DoctorDto transfer(Long id, String newDepartmentCode) {
        Doctor entity = doctorRepository.findById(id).orElseThrow(() -> NotFoundException.of("Medico", id));
        String deptCode = normalizeCode(newDepartmentCode, "Reparto non valido.");
        Department newDepartment = resolveDepartment(deptCode);
        entity.setDepartment(newDepartment);
        Doctor saved = doctorRepository.save(entity);
        return doctorMapper.toDto(saved);
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
    public Page<DoctorDto> search(String q, String departmentCode, String facilityCode, String status,
                                  int page, int size, String[] sort) {

        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.DOCTOR_ALLOWED, "id");
        Pageable pageable = PageableUtils.pageRequest(page, size, MAX_PAGE_SIZE, safeSort);

        UserStatus userStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Ignora filtro status non valido
            }
        }

        Page<Doctor> result = doctorRepository.findAll(
                DoctorSpecifications.search(q, departmentCode, facilityCode, userStatus),
                pageable
        );

        return result.map(doctorMapper::toDto);
    }

    private Department resolveDepartment(String deptCode) {
        return departmentRepository.findByCodeIgnoreCase(deptCode)
                .orElseThrow(() -> new IllegalArgumentException("Reparto non valido: " + deptCode));
    }

    private String normalizeEmail(String email) {
        if (Objects.isNull(email)) throw new IllegalArgumentException("Email obbligatoria.");
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (Objects.isNull(phone)) {
            return null;
        }
        String normalized = phone.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeCode(String code, String errorMessage) {
        if (Objects.isNull(code)) {
            throw new IllegalArgumentException(errorMessage);
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }
}
