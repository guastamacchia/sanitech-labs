package it.sanitech.directory.services;

import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.integrations.keycloak.KeycloakAdminClient;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.DoctorRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Doctor;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import it.sanitech.directory.services.events.KeycloakUserSyncEvent;
import it.sanitech.directory.services.mapper.DoctorMapper;
import it.sanitech.outbox.core.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DeptGuard deptGuard;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void shouldCreateDoctor() {
        DoctorCreateDto dto = TestDataFactory.doctorCreateDtoWithMixedCaseEmail();
        Department department = TestDataFactory.cardiologyDepartment();
        Doctor saved = TestDataFactory.doctorEntity(11L, "Luca", "Bianchi", "luca.bianchi@email.it");
        saved.setDepartment(department);
        DoctorDto mappedDto = TestDataFactory.doctorDto(
                11L,
                "Luca",
                "Bianchi",
                "luca.bianchi@email.it"
        );

        when(doctorRepository.existsByEmailIgnoreCase("luca.bianchi@email.it")).thenReturn(false);
        when(departmentRepository.findByCodeIgnoreCase("CARD")).thenReturn(Optional.of(department));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(saved);
        when(doctorMapper.toDto(saved)).thenReturn(mappedDto);

        DoctorDto result = doctorService.create(dto, null);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor entity = captor.getValue();

        assertThat(entity.getEmail()).isEqualTo("luca.bianchi@email.it");
        assertThat(entity.getDepartment()).isEqualTo(department);
        assertThat(result).isEqualTo(mappedDto);
        verify(deptGuard).checkCanManageAll(Set.of("CARD"), null);

        ArgumentCaptor<KeycloakUserSyncEvent> eventCaptor = ArgumentCaptor.forClass(KeycloakUserSyncEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        KeycloakUserSyncEvent syncEvent = eventCaptor.getValue();
        assertThat(syncEvent.aggregateType()).isEqualTo("DOCTOR");
        assertThat(syncEvent.aggregateId()).isEqualTo(11L);
        assertThat(syncEvent.email()).isEqualTo("luca.bianchi@email.it");
        assertThat(syncEvent.firstName()).isEqualTo("Luca");
        assertThat(syncEvent.lastName()).isEqualTo("Bianchi");
        assertThat(syncEvent.phone()).isNull();
        assertThat(syncEvent.enabled()).isFalse();  // Utente disabilitato fino ad attivazione admin
        assertThat(syncEvent.roleToAssign()).isEqualTo("ROLE_DOCTOR");
        assertThat(syncEvent.previousEmail()).isNull();

        verify(eventPublisher).publish(eq("DOCTOR"), eq("11"), eq("DOCTOR_CREATED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void shouldPatchDoctor() {
        DoctorUpdateDto dto = TestDataFactory.doctorUpdateDto();
        Department department = TestDataFactory.cardiologyDepartment();
        Doctor existing = TestDataFactory.doctorEntity(9L, " Luca ", " Bianchi ", "vecchia@email.it");
        Doctor saved = TestDataFactory.doctorEntity(9L, "Luca", "Bianchi", "nuova.email@email.it");
        saved.setDepartment(department);
        DoctorDto mappedDto = TestDataFactory.doctorDto(
                9L,
                "Luca",
                "Bianchi",
                "nuova.email@email.it"
        );

        when(doctorRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByCodeIgnoreCase("CARD")).thenReturn(Optional.of(department));
        doAnswer(invocation -> {
            DoctorUpdateDto update = invocation.getArgument(0);
            Doctor entity = invocation.getArgument(1);
            entity.setFirstName(update.firstName());
            entity.setLastName(update.lastName());
            entity.setEmail(update.email());
            return null;
        }).when(doctorMapper).updateEntity(eq(dto), eq(existing));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(saved);
        when(doctorMapper.toDto(saved)).thenReturn(mappedDto);

        DoctorDto result = doctorService.patch(9L, dto, null);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor entity = captor.getValue();

        assertThat(entity.getEmail()).isEqualTo("nuova.email@email.it");
        assertThat(entity.getDepartment()).isEqualTo(department);
        assertThat(result).isEqualTo(mappedDto);
        verify(deptGuard).checkCanManageAll(Set.of("CARD"), null);

        ArgumentCaptor<KeycloakUserSyncEvent> eventCaptor = ArgumentCaptor.forClass(KeycloakUserSyncEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        KeycloakUserSyncEvent syncEvent = eventCaptor.getValue();
        assertThat(syncEvent.aggregateType()).isEqualTo("DOCTOR");
        assertThat(syncEvent.aggregateId()).isEqualTo(9L);
        assertThat(syncEvent.email()).isEqualTo("nuova.email@email.it");
        assertThat(syncEvent.firstName()).isEqualTo("Luca");
        assertThat(syncEvent.lastName()).isEqualTo("Bianchi");
        assertThat(syncEvent.phone()).isNull();
        assertThat(syncEvent.enabled()).isTrue();
        assertThat(syncEvent.roleToAssign()).isNull();
        assertThat(syncEvent.previousEmail()).isEqualTo("vecchia@email.it");

        verify(eventPublisher).publish(eq("DOCTOR"), eq("9"), eq("DOCTOR_UPDATED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void shouldDeleteDoctor() {
        Doctor existing = TestDataFactory.doctorEntity(4L, "Paola", "Neri", "paola.neri@email.it");

        when(doctorRepository.findById(4L)).thenReturn(Optional.of(existing));

        doctorService.delete(4L, null);

        verify(keycloakAdminClient).disableUser("paola.neri@email.it", "Paola", "Neri", null, "DOCTOR", 4L);
        verify(doctorRepository).delete(existing);
        verify(eventPublisher).publish(eq("DOCTOR"), eq("4"), eq("DOCTOR_DELETED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void shouldGetDoctor() {
        Doctor existing = TestDataFactory.doctorEntity(2L, "Sara", "Verdi", "sara.verdi@email.it");
        DoctorDto mappedDto = TestDataFactory.doctorDto(2L, "Sara", "Verdi", "sara.verdi@email.it");

        when(doctorRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(doctorMapper.toDto(existing)).thenReturn(mappedDto);

        DoctorDto result = doctorService.get(2L);

        assertThat(result).isEqualTo(mappedDto);
        verify(doctorRepository).findById(2L);
    }

    @Test
    void shouldSearchDoctors() {
        Doctor doctor = TestDataFactory.doctorEntity(3L, "Marco", "Rossi", "marco.rossi@email.it");
        DoctorDto mappedDto = TestDataFactory.doctorDto(3L, "Marco", "Rossi", "marco.rossi@email.it");

        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doctor)));
        when(doctorMapper.toDto(doctor)).thenReturn(mappedDto);

        var result = doctorService.search("Rossi", "CARD", "HOSP_CENTRAL", null, 0, 20, null);

        assertThat(result.getContent()).containsExactly(mappedDto);
    }
}
