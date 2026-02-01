package it.sanitech.prescribing.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.prescribing.integrations.consents.ConsentClient;
import it.sanitech.prescribing.repositories.PrescriptionRepository;
import it.sanitech.prescribing.repositories.entities.Prescription;
import it.sanitech.prescribing.repositories.entities.PrescriptionItem;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.PrescriptionItemDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionCreateDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionItemCreateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionPatchDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionItemUpdateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionUpdateDto;
import it.sanitech.prescribing.services.mappers.PrescriptionMapper;
import it.sanitech.prescribing.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class PrescriptionServiceTest {

    @Test
    void createSetsDoctorAndPublishesEvent() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        PrescriptionCreateDto dto = new PrescriptionCreateDto(
                77L,
                "CARDIO",
                "note",
                List.of(new PrescriptionItemCreateDto("MED-1", "Aspirin", "1cp", "1/die", 7, "after meal", 1))
        );
        Prescription entity = Prescription.builder()
                .patientId(77L)
                .departmentCode("CARDIO")
                .status(PrescriptionStatus.DRAFT)
                .build();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(mapper.toEntity(any(PrescriptionItemCreateDto.class)))
                .thenReturn(PrescriptionItem.builder().medicationName("Aspirin").build());
        when(repository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(mapper.toDto(any(Prescription.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = doctorAuth(55L);

        PrescriptionDto result = service.create(dto, auth);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(result.doctorId()).isEqualTo(55L);

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDoctorId()).isEqualTo(55L);
        assertThat(captor.getValue().getItems()).hasSize(1);

        verify(deptGuard).checkCanManage(eq("CARDIO"), eq(auth));
        verify(consentClient).assertPrescriptionConsent(eq(77L), eq(55L), eq(auth));
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_PRESCRIPTION), eq("10"), eq(AppConstants.Outbox.EVT_PRESCRIPTION_CREATED), any());
    }

    @Test
    void listMineUsesStatusFilter() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        Prescription entity = Prescription.builder()
                .id(20L)
                .patientId(44L)
                .doctorId(55L)
                .departmentCode("CARDIO")
                .status(PrescriptionStatus.ISSUED)
                .build();
        when(repository.findByPatientIdAndStatus(eq(44L), eq(PrescriptionStatus.ISSUED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 1));
        when(mapper.toDto(any(Prescription.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = patientAuth(44L);

        Page<PrescriptionDto> page = service.listMine(PrescriptionStatus.ISSUED, PageRequest.of(0, 1), auth);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(20L);
    }

    @Test
    void getForDoctorChecksConsentAndDepartment() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        Prescription entity = Prescription.builder()
                .id(30L)
                .patientId(77L)
                .doctorId(55L)
                .departmentCode("NEURO")
                .status(PrescriptionStatus.ISSUED)
                .build();
        when(repository.findDetailedById(30L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(toDto(entity));

        JwtAuthenticationToken auth = doctorAuth(55L);

        PrescriptionDto result = service.getForDoctor(30L, auth);

        assertThat(result.id()).isEqualTo(30L);
        verify(deptGuard).checkCanManage(eq("NEURO"), eq(auth));
        verify(consentClient).assertPrescriptionConsent(eq(77L), eq(55L), eq(auth));
    }

    @Test
    void patchPublishesUpdateEvent() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        Prescription entity = Prescription.builder()
                .id(40L)
                .patientId(77L)
                .doctorId(55L)
                .departmentCode("CARDIO")
                .status(PrescriptionStatus.ISSUED)
                .build();
        when(repository.findDetailedById(40L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Prescription.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = doctorAuth(55L);

        PrescriptionDto result = service.patch(40L, new PrescriptionPatchDto("updated"), auth);

        assertThat(result.id()).isEqualTo(40L);
        verify(mapper).patch(eq(entity), any(PrescriptionPatchDto.class));
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_PRESCRIPTION), eq("40"), eq(AppConstants.Outbox.EVT_PRESCRIPTION_UPDATED), any());
    }

    @Test
    void updateReplacesItemsAndPublishesEvent() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        Prescription entity = Prescription.builder()
                .id(50L)
                .patientId(77L)
                .doctorId(55L)
                .departmentCode("CARDIO")
                .status(PrescriptionStatus.ISSUED)
                .build();
        when(repository.findDetailedById(50L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toEntity(any(PrescriptionItemUpdateDto.class)))
                .thenReturn(PrescriptionItem.builder().medicationName("Updated").build());
        when(mapper.toDto(any(Prescription.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = doctorAuth(55L);

        PrescriptionUpdateDto updateDto = new PrescriptionUpdateDto(
                "new notes",
                List.of(new PrescriptionItemUpdateDto("MED-2", "Updated", "2cp", "2/die", 10, "notes", 1))
        );
        PrescriptionDto result = service.update(50L, updateDto, auth);

        assertThat(result.id()).isEqualTo(50L);
        assertThat(entity.getItems()).hasSize(1);
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_PRESCRIPTION), eq("50"), eq(AppConstants.Outbox.EVT_PRESCRIPTION_UPDATED), any());
    }

    @Test
    void cancelMarksAndPublishesEvent() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        Prescription entity = Prescription.builder()
                .id(60L)
                .patientId(77L)
                .doctorId(55L)
                .departmentCode("CARDIO")
                .status(PrescriptionStatus.ISSUED)
                .build();
        when(repository.findDetailedById(60L)).thenReturn(Optional.of(entity));

        JwtAuthenticationToken auth = doctorAuth(55L);

        service.cancel(60L, auth);

        assertThat(entity.getStatus()).isEqualTo(PrescriptionStatus.CANCELLED);
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_PRESCRIPTION), eq("60"), eq(AppConstants.Outbox.EVT_PRESCRIPTION_CANCELLED), any());
    }

    @Test
    void adminGetThrowsWhenMissing() {
        PrescriptionRepository repository = Mockito.mock(PrescriptionRepository.class);
        PrescriptionMapper mapper = Mockito.mock(PrescriptionMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);

        PrescriptionService service = new PrescriptionService(repository, mapper, events, consentClient, deptGuard);

        when(repository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.adminGet(99L))
                .isInstanceOf(NotFoundException.class);
    }

    private static JwtAuthenticationToken doctorAuth(Long doctorId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(AppConstants.JwtClaim.DOCTOR_ID, doctorId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DOCTOR")));
    }

    private static JwtAuthenticationToken patientAuth(Long patientId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(AppConstants.JwtClaim.PATIENT_ID, patientId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    private static PrescriptionDto toDto(Prescription entity) {
        return new PrescriptionDto(
                entity.getId(),
                entity.getPatientId(),
                entity.getDoctorId(),
                entity.getDepartmentCode(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIssuedAt(),
                entity.getCancelledAt(),
                List.of(new PrescriptionItemDto(1L, "MED-1", "Aspirin", "1cp", "1/die", 7, "after meal", 1))
        );
    }
}
