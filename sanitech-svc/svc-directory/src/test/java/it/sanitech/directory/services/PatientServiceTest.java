package it.sanitech.directory.services;

import it.sanitech.commons.security.DeptGuard;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.integrations.keycloak.KeycloakAdminClient;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.PatientRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Patient;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.services.events.KeycloakUserSyncEvent;
import it.sanitech.directory.services.mapper.PatientMapper;
import it.sanitech.outbox.core.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DeptGuard deptGuard;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @InjectMocks
    private PatientService patientService;

    @Test
    void shouldCreatePatient() {
        PatientCreateDto dto = TestDataFactory.patientCreateDtoWithMixedCaseEmail();
        Department department = TestDataFactory.cardiologyDepartment();
        Patient saved = TestDataFactory.patientEntity(
                1L,
                "Mario",
                "Rossi",
                "nome.cognome@email.it",
                "+39 333 123 4567"
        );
        saved.setDepartments(Set.of(department));
        PatientDto mappedDto = TestDataFactory.patientDto(
                1L,
                "Mario",
                "Rossi",
                "nome.cognome@email.it",
                "+39 333 123 4567"
        );

        when(patientRepository.existsByEmailIgnoreCase("nome.cognome@email.it")).thenReturn(false);
        when(departmentRepository.findByCodeIn(Set.of("CARD"))).thenReturn(List.of(department));
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);
        when(patientMapper.toDto(saved)).thenReturn(mappedDto);

        PatientDto result = patientService.create(dto, null);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient entity = captor.getValue();

        assertThat(entity.getFirstName()).isEqualTo("Mario");
        assertThat(entity.getLastName()).isEqualTo("Rossi");
        assertThat(entity.getEmail()).isEqualTo("nome.cognome@email.it");
        assertThat(entity.getDepartments()).contains(department);
        assertThat(result).isEqualTo(mappedDto);

        ArgumentCaptor<KeycloakUserSyncEvent> eventCaptor = ArgumentCaptor.forClass(KeycloakUserSyncEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        KeycloakUserSyncEvent syncEvent = eventCaptor.getValue();
        assertThat(syncEvent.aggregateType()).isEqualTo("PATIENT");
        assertThat(syncEvent.aggregateId()).isEqualTo(1L);
        assertThat(syncEvent.email()).isEqualTo("nome.cognome@email.it");
        assertThat(syncEvent.firstName()).isEqualTo("Mario");
        assertThat(syncEvent.lastName()).isEqualTo("Rossi");
        assertThat(syncEvent.phone()).isEqualTo("+39 333 123 4567");
        assertThat(syncEvent.enabled()).isTrue();
        assertThat(syncEvent.roleToAssign()).isEqualTo("ROLE_PATIENT");
        assertThat(syncEvent.previousEmail()).isNull();

        verify(eventPublisher).publish(eq("PATIENT"), eq("1"), eq("PATIENT_CREATED"), any());
    }

    @Test
    void shouldPatchPatient() {
        PatientUpdateDto dto = TestDataFactory.patientUpdateDtoWithWhitespace();
        Patient existing = TestDataFactory.patientEntity(
                7L,
                " Mario ",
                " Rossi ",
                "vecchia@email.it",
                " 333 111 222 "
        );
        Patient saved = TestDataFactory.patientEntity(
                7L,
                "Maria",
                "Rossi",
                "nuova.email@email.it",
                "333 444 555"
        );
        PatientDto mappedDto = TestDataFactory.patientDto(
                7L,
                "Maria",
                "Rossi",
                "nuova.email@email.it",
                "333 444 555"
        );

        when(patientRepository.findById(7L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            PatientUpdateDto update = invocation.getArgument(0);
            Patient entity = invocation.getArgument(1);
            entity.setFirstName(update.firstName());
            entity.setLastName(update.lastName());
            entity.setEmail(update.email());
            entity.setPhone(update.phone());
            return null;
        }).when(patientMapper).updateEntity(eq(dto), eq(existing));
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);
        when(patientMapper.toDto(saved)).thenReturn(mappedDto);

        PatientDto result = patientService.patch(7L, dto, null);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        Patient entity = captor.getValue();

        assertThat(entity.getFirstName()).isEqualTo("Maria");
        assertThat(entity.getEmail()).isEqualTo("nuova.email@email.it");
        verify(deptGuard, never()).checkCanManageAll(any(), any());
        assertThat(result).isEqualTo(mappedDto);

        ArgumentCaptor<KeycloakUserSyncEvent> eventCaptor = ArgumentCaptor.forClass(KeycloakUserSyncEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        KeycloakUserSyncEvent syncEvent = eventCaptor.getValue();
        assertThat(syncEvent.aggregateType()).isEqualTo("PATIENT");
        assertThat(syncEvent.aggregateId()).isEqualTo(7L);
        assertThat(syncEvent.email()).isEqualTo("nuova.email@email.it");
        assertThat(syncEvent.firstName()).isEqualTo("Maria");
        assertThat(syncEvent.lastName()).isEqualTo("Rossi");
        assertThat(syncEvent.phone()).isEqualTo("333 444 555");
        assertThat(syncEvent.enabled()).isTrue();
        assertThat(syncEvent.roleToAssign()).isNull();
        assertThat(syncEvent.previousEmail()).isEqualTo("vecchia@email.it");

        verify(eventPublisher).publish(eq("PATIENT"), eq("7"), eq("PATIENT_UPDATED"), any());
    }

    @Test
    void shouldDeletePatient() {
        Patient existing = TestDataFactory.patientEntity(
                5L,
                "Paolo",
                "Verdi",
                "paolo.verdi@email.it",
                null
        );

        when(patientRepository.findById(5L)).thenReturn(Optional.of(existing));

        patientService.delete(5L);

        verify(keycloakAdminClient).disableUser("paolo.verdi@email.it");
        verify(patientRepository).delete(existing);
        verify(eventPublisher).publish(eq("PATIENT"), eq("5"), eq("PATIENT_DELETED"), any());
    }

    @Test
    void shouldGetPatient() {
        Patient existing = TestDataFactory.patientEntity(
                2L,
                "Laura",
                "Neri",
                "laura.neri@email.it",
                null
        );
        PatientDto mappedDto = TestDataFactory.patientDto(
                2L,
                "Laura",
                "Neri",
                "laura.neri@email.it",
                null
        );

        when(patientRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(patientMapper.toDto(existing)).thenReturn(mappedDto);

        PatientDto result = patientService.get(2L);

        assertThat(result).isEqualTo(mappedDto);
        verify(patientRepository).findById(2L);
    }
}
