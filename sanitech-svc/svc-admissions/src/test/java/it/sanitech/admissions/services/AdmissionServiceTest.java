package it.sanitech.admissions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.admissions.exception.NoBedAvailableException;
import it.sanitech.admissions.repositories.AdmissionRepository;
import it.sanitech.admissions.repositories.DepartmentCapacityRepository;
import it.sanitech.admissions.repositories.entities.Admission;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.repositories.entities.AdmissionType;
import it.sanitech.admissions.repositories.entities.DepartmentCapacity;
import it.sanitech.admissions.services.dto.AdmissionDto;
import it.sanitech.admissions.services.dto.create.AdmissionCreateDto;
import it.sanitech.admissions.services.mapper.AdmissionMapper;
import it.sanitech.commons.exception.ConflictException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class AdmissionServiceTest {

    @Test
    void admitPublishesEventWhenCapacityAvailable() {
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionMapper mapper = Mockito.mock(AdmissionMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        AdmissionService service = new AdmissionService(admissions, capacityRepository, mapper, deptGuard, domainEvents);

        AdmissionCreateDto dto = new AdmissionCreateDto(10L, "cardio", AdmissionType.INPATIENT, "note", 22L);
        DepartmentCapacity capacity = new DepartmentCapacity("CARDIO", 2, Instant.parse("2024-01-01T00:00:00Z"));

        when(capacityRepository.lockByDeptCode("CARDIO")).thenReturn(Optional.of(capacity));
        when(admissions.countByDepartmentCodeIgnoreCaseAndStatus("CARDIO", AdmissionStatus.ACTIVE))
                .thenReturn(1L);

        Admission admission = new Admission();
        admission.setPatientId(10L);
        admission.setAdmissionType(AdmissionType.INPATIENT);
        admission.setNotes("note");
        admission.setAttendingDoctorId(22L);
        when(mapper.toEntity(dto)).thenReturn(admission);
        when(admissions.save(any(Admission.class))).thenAnswer(invocation -> {
            Admission saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });
        when(mapper.toDto(any(Admission.class))).thenAnswer(invocation -> {
            Admission saved = invocation.getArgument(0);
            return new AdmissionDto(
                    saved.getId(),
                    saved.getPatientId(),
                    saved.getDepartmentCode(),
                    saved.getAdmissionType(),
                    saved.getStatus(),
                    saved.getAdmittedAt(),
                    saved.getDischargedAt(),
                    saved.getNotes(),
                    saved.getAttendingDoctorId()
            );
        });

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        AdmissionDto result = service.admit(dto, auth);

        assertThat(result.id()).isEqualTo(77L);
        assertThat(result.departmentCode()).isEqualTo("CARDIO");
        assertThat(result.status()).isEqualTo(AdmissionStatus.ACTIVE);

        ArgumentCaptor<Admission> admissionCaptor = ArgumentCaptor.forClass(Admission.class);
        verify(admissions).save(admissionCaptor.capture());
        assertThat(admissionCaptor.getValue().getAdmittedAt()).isNotNull();
        assertThat(admissionCaptor.getValue().getDepartmentCode()).isEqualTo("CARDIO");

        verify(deptGuard).checkCanManage("CARDIO", auth);
        verify(domainEvents).publish(
                eq("ADMISSION"),
                eq("77"),
                eq("ADMISSION_CREATED"),
                any(Map.class)
        );
    }

    @Test
    void admitThrowsWhenNoBedsAvailable() {
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionMapper mapper = Mockito.mock(AdmissionMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        AdmissionService service = new AdmissionService(admissions, capacityRepository, mapper, deptGuard, domainEvents);

        AdmissionCreateDto dto = new AdmissionCreateDto(10L, "cardio", AdmissionType.INPATIENT, null, null);
        DepartmentCapacity capacity = new DepartmentCapacity("CARDIO", 1, Instant.now());

        when(capacityRepository.lockByDeptCode("CARDIO")).thenReturn(Optional.of(capacity));
        when(admissions.countByDepartmentCodeIgnoreCaseAndStatus("CARDIO", AdmissionStatus.ACTIVE))
                .thenReturn(1L);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        assertThatThrownBy(() -> service.admit(dto, auth))
                .isInstanceOf(NoBedAvailableException.class);
    }

    @Test
    void dischargeUpdatesStatusAndPublishesEvent() {
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionMapper mapper = Mockito.mock(AdmissionMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        AdmissionService service = new AdmissionService(admissions, capacityRepository, mapper, deptGuard, domainEvents);

        Admission existing = new Admission();
        existing.setId(5L);
        existing.setPatientId(10L);
        existing.setDepartmentCode("WARD");
        existing.setAdmissionType(AdmissionType.OBSERVATION);
        existing.setStatus(AdmissionStatus.ACTIVE);
        existing.setAdmittedAt(Instant.parse("2024-01-10T00:00:00Z"));

        when(admissions.findById(5L)).thenReturn(Optional.of(existing));
        when(admissions.save(any(Admission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Admission.class))).thenAnswer(invocation -> {
            Admission saved = invocation.getArgument(0);
            return new AdmissionDto(
                    saved.getId(),
                    saved.getPatientId(),
                    saved.getDepartmentCode(),
                    saved.getAdmissionType(),
                    saved.getStatus(),
                    saved.getAdmittedAt(),
                    saved.getDischargedAt(),
                    saved.getNotes(),
                    saved.getAttendingDoctorId()
            );
        });

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        AdmissionDto result = service.discharge(5L, auth);

        assertThat(result.status()).isEqualTo(AdmissionStatus.DISCHARGED);
        assertThat(result.dischargedAt()).isNotNull();

        ArgumentCaptor<Admission> admissionCaptor = ArgumentCaptor.forClass(Admission.class);
        verify(admissions).save(admissionCaptor.capture());
        assertThat(admissionCaptor.getValue().getStatus()).isEqualTo(AdmissionStatus.DISCHARGED);

        verify(deptGuard).checkCanManage("WARD", auth);
        verify(domainEvents).publish(
                eq("ADMISSION"),
                eq("5"),
                eq("ADMISSION_DISCHARGED"),
                any(Map.class)
        );
    }

    @Test
    void listForAdminUsesRepository() {
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionMapper mapper = Mockito.mock(AdmissionMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        AdmissionService service = new AdmissionService(admissions, capacityRepository, mapper, deptGuard, domainEvents);

        Admission admission = new Admission();
        admission.setId(9L);
        admission.setDepartmentCode("WARD");
        admission.setAdmissionType(AdmissionType.DAY_HOSPITAL);
        admission.setStatus(AdmissionStatus.ACTIVE);
        Page<Admission> page = new PageImpl<>(java.util.List.of(admission), PageRequest.of(0, 10), 1);

        when(admissions.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(any(Admission.class))).thenAnswer(invocation -> {
            Admission saved = invocation.getArgument(0);
            return new AdmissionDto(
                    saved.getId(),
                    saved.getPatientId(),
                    saved.getDepartmentCode(),
                    saved.getAdmissionType(),
                    saved.getStatus(),
                    saved.getAdmittedAt(),
                    saved.getDischargedAt(),
                    saved.getNotes(),
                    saved.getAttendingDoctorId()
            );
        });

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        Page<AdmissionDto> result = service.list(auth, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(admissions).findAll(any(Pageable.class));
    }

    @Test
    void dischargeRejectsNonActiveAdmissions() {
        AdmissionRepository admissions = Mockito.mock(AdmissionRepository.class);
        DepartmentCapacityRepository capacityRepository = Mockito.mock(DepartmentCapacityRepository.class);
        AdmissionMapper mapper = Mockito.mock(AdmissionMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher domainEvents = Mockito.mock(DomainEventPublisher.class);

        AdmissionService service = new AdmissionService(admissions, capacityRepository, mapper, deptGuard, domainEvents);

        Admission existing = new Admission();
        existing.setId(5L);
        existing.setDepartmentCode("WARD");
        existing.setAdmissionType(AdmissionType.OBSERVATION);
        existing.setStatus(AdmissionStatus.DISCHARGED);

        when(admissions.findById(5L)).thenReturn(Optional.of(existing));

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        assertThatThrownBy(() -> service.discharge(5L, auth))
                .isInstanceOf(ConflictException.class);
    }
}
