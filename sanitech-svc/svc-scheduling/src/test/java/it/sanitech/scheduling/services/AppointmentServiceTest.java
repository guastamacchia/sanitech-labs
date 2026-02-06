package it.sanitech.scheduling.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.scheduling.repositories.AppointmentRepository;
import it.sanitech.scheduling.repositories.SlotRepository;
import it.sanitech.scheduling.repositories.entities.Appointment;
import it.sanitech.scheduling.repositories.entities.AppointmentStatus;
import it.sanitech.scheduling.repositories.entities.Slot;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import it.sanitech.scheduling.services.dto.create.AppointmentCreateDto;
import it.sanitech.scheduling.services.dto.update.AppointmentReassignDto;
import it.sanitech.scheduling.services.dto.update.AppointmentRescheduleDto;
import it.sanitech.scheduling.services.mapper.AppointmentMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AppointmentServiceTest {

    @Test
    void bookCreatesAppointmentAndPublishesEvent() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);

        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Slot slot = Slot.builder()
                .id(11L)
                .doctorId(22L)
                .departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON)
                .startAt(Instant.parse("2024-01-01T10:00:00Z"))
                .endAt(Instant.parse("2024-01-01T10:30:00Z"))
                .status(SlotStatus.AVAILABLE)
                .build();
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));
        when(slots.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointments.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(33L);
            return saved;
        });
        when(mapper.toDto(any(Appointment.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = patientAuth(77L);
        AppointmentDto result = service.book(new AppointmentCreateDto(11L, null, null), auth);

        assertThat(result.id()).isEqualTo(33L);
        assertThat(result.status()).isEqualTo(AppointmentStatus.BOOKED);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointments).save(appointmentCaptor.capture());
        assertThat(appointmentCaptor.getValue().getPatientId()).isEqualTo(77L);

        verify(events).publish(eq("APPOINTMENT"), eq("33"), eq("APPOINTMENT_BOOKED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void bookRejectsUnavailableSlot() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);

        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Slot slot = Slot.builder()
                .id(11L)
                .status(SlotStatus.BOOKED)
                .build();
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));

        JwtAuthenticationToken auth = patientAuth(77L);

        assertThatThrownBy(() -> service.book(new AppointmentCreateDto(11L, null, null), auth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_SLOT_NOT_AVAILABLE);
    }

    @Test
    void searchForAdminReturnsPage() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);

        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment entity = Appointment.builder()
                .id(50L)
                .slotId(11L)
                .patientId(77L)
                .doctorId(22L)
                .departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON)
                .startAt(Instant.parse("2024-01-01T10:00:00Z"))
                .endAt(Instant.parse("2024-01-01T10:30:00Z"))
                .status(AppointmentStatus.BOOKED)
                .build();
        when(appointments.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 1));
        when(mapper.toDto(any(Appointment.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = adminAuth();
        Page<AppointmentDto> page = service.search(77L, 22L, "CARDIO", 0, 1, new String[]{"startAt,desc"}, auth);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(50L);
    }

    @Test
    void cancelReleasesSlotAndPublishesEvent() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);

        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appointment = Appointment.builder()
                .id(60L)
                .slotId(11L)
                .patientId(77L)
                .status(AppointmentStatus.BOOKED)
                .build();
        Slot slot = Slot.builder()
                .id(11L)
                .status(SlotStatus.BOOKED)
                .build();
        when(appointments.findById(60L)).thenReturn(Optional.of(appointment));
        when(appointments.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(slot));
        when(slots.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JwtAuthenticationToken auth = patientAuth(77L);
        service.cancel(60L, auth);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        verify(events).publish(eq("APPOINTMENT"), eq("60"), eq("APPOINTMENT_CANCELLED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void cancelThrowsWhenMissing() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);

        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        when(appointments.findById(99L)).thenReturn(Optional.empty());

        JwtAuthenticationToken auth = adminAuth();

        assertThatThrownBy(() -> service.cancel(99L, auth))
                .isInstanceOf(NotFoundException.class);
    }

    // ── Reschedule tests ──

    @Test
    void rescheduleMovesToNewSlotAndPublishesEvent() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder()
                .id(60L).slotId(11L).patientId(77L).doctorId(22L)
                .departmentCode("CARDIO").mode(VisitMode.IN_PERSON)
                .startAt(Instant.parse("2024-01-01T10:00:00Z")).endAt(Instant.parse("2024-01-01T10:30:00Z"))
                .status(AppointmentStatus.BOOKED).build();
        Slot oldSlot = Slot.builder().id(11L).doctorId(22L).departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON).status(SlotStatus.BOOKED)
                .startAt(Instant.parse("2024-01-01T10:00:00Z")).endAt(Instant.parse("2024-01-01T10:30:00Z")).build();
        Slot newSlot = Slot.builder().id(12L).doctorId(22L).departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON).status(SlotStatus.AVAILABLE)
                .startAt(Instant.parse("2024-01-02T09:00:00Z")).endAt(Instant.parse("2024-01-02T09:30:00Z")).build();

        when(appointments.findById(60L)).thenReturn(Optional.of(appt));
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(oldSlot));
        when(slots.findByIdForUpdate(12L)).thenReturn(Optional.of(newSlot));
        when(slots.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));
        when(appointments.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any(Appointment.class))).thenAnswer(i -> toDto(i.getArgument(0)));

        JwtAuthenticationToken auth = adminAuth();
        AppointmentDto result = service.reschedule(60L, new AppointmentRescheduleDto(12L), auth);

        assertThat(result.slotId()).isEqualTo(12L);
        assertThat(result.startAt()).isEqualTo(Instant.parse("2024-01-02T09:00:00Z"));
        assertThat(oldSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(newSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(events).publish(eq("APPOINTMENT"), eq("60"), eq("APPOINTMENT_RESCHEDULED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void rescheduleRejectsNonBookedAppointment() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder().id(60L).slotId(11L).status(AppointmentStatus.CANCELLED).build();
        when(appointments.findById(60L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.reschedule(60L, new AppointmentRescheduleDto(12L), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_APPOINTMENT_NOT_BOOKED);
    }

    @Test
    void rescheduleRejectsSameSlot() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder().id(60L).slotId(11L).status(AppointmentStatus.BOOKED).build();
        when(appointments.findById(60L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.reschedule(60L, new AppointmentRescheduleDto(11L), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_SAME_SLOT);
    }

    @Test
    void rescheduleRejectsDifferentDoctor() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder().id(60L).slotId(11L).doctorId(22L)
                .mode(VisitMode.IN_PERSON).status(AppointmentStatus.BOOKED).build();
        Slot oldSlot = Slot.builder().id(11L).doctorId(22L).status(SlotStatus.BOOKED).build();
        Slot newSlot = Slot.builder().id(12L).doctorId(99L).mode(VisitMode.IN_PERSON)
                .status(SlotStatus.AVAILABLE).build();

        when(appointments.findById(60L)).thenReturn(Optional.of(appt));
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(oldSlot));
        when(slots.findByIdForUpdate(12L)).thenReturn(Optional.of(newSlot));
        when(slots.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> service.reschedule(60L, new AppointmentRescheduleDto(12L), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_SLOT_DOCTOR_MISMATCH);
    }

    // ── Reassign tests ──

    @Test
    void reassignMovesToNewDoctorAndPublishesEvent() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder()
                .id(60L).slotId(11L).patientId(77L).doctorId(22L)
                .departmentCode("CARDIO").mode(VisitMode.IN_PERSON)
                .startAt(Instant.parse("2024-01-01T10:00:00Z")).endAt(Instant.parse("2024-01-01T10:30:00Z"))
                .status(AppointmentStatus.BOOKED).build();
        Slot oldSlot = Slot.builder().id(11L).doctorId(22L).departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON).status(SlotStatus.BOOKED)
                .startAt(Instant.parse("2024-01-01T10:00:00Z")).endAt(Instant.parse("2024-01-01T10:30:00Z")).build();
        Slot newSlot = Slot.builder().id(20L).doctorId(33L).departmentCode("NEURO")
                .mode(VisitMode.IN_PERSON).status(SlotStatus.AVAILABLE)
                .startAt(Instant.parse("2024-01-03T14:00:00Z")).endAt(Instant.parse("2024-01-03T14:30:00Z")).build();

        when(appointments.findById(60L)).thenReturn(Optional.of(appt));
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(oldSlot));
        when(slots.findByIdForUpdate(20L)).thenReturn(Optional.of(newSlot));
        when(slots.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));
        when(appointments.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toDto(any(Appointment.class))).thenAnswer(i -> toDto(i.getArgument(0)));

        JwtAuthenticationToken auth = adminAuth();
        AppointmentDto result = service.reassign(60L, new AppointmentReassignDto(33L, 20L), auth);

        assertThat(result.doctorId()).isEqualTo(33L);
        assertThat(result.departmentCode()).isEqualTo("NEURO");
        assertThat(result.slotId()).isEqualTo(20L);
        assertThat(result.startAt()).isEqualTo(Instant.parse("2024-01-03T14:00:00Z"));
        assertThat(oldSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(newSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(events).publish(eq("APPOINTMENT"), eq("60"), eq("APPOINTMENT_REASSIGNED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void reassignRejectsNonBookedAppointment() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder().id(60L).slotId(11L).status(AppointmentStatus.COMPLETED).build();
        when(appointments.findById(60L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() -> service.reassign(60L, new AppointmentReassignDto(33L, 20L), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_APPOINTMENT_NOT_BOOKED);
    }

    @Test
    void reassignRejectsDoctorSlotMismatch() {
        AppointmentRepository appointments = Mockito.mock(AppointmentRepository.class);
        SlotRepository slots = Mockito.mock(SlotRepository.class);
        AppointmentMapper mapper = Mockito.mock(AppointmentMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        it.sanitech.scheduling.clients.DirectoryClient directoryClient = Mockito.mock(it.sanitech.scheduling.clients.DirectoryClient.class);
        AppointmentService service = new AppointmentService(appointments, slots, mapper, events, directoryClient);

        Appointment appt = Appointment.builder().id(60L).slotId(11L).doctorId(22L)
                .mode(VisitMode.IN_PERSON).status(AppointmentStatus.BOOKED).build();
        Slot oldSlot = Slot.builder().id(11L).doctorId(22L).status(SlotStatus.BOOKED).build();
        Slot newSlot = Slot.builder().id(20L).doctorId(44L).mode(VisitMode.IN_PERSON)
                .status(SlotStatus.AVAILABLE).build();

        when(appointments.findById(60L)).thenReturn(Optional.of(appt));
        when(slots.findByIdForUpdate(11L)).thenReturn(Optional.of(oldSlot));
        when(slots.findByIdForUpdate(20L)).thenReturn(Optional.of(newSlot));
        when(slots.save(any(Slot.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> service.reassign(60L, new AppointmentReassignDto(33L, 20L), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_NEW_DOCTOR_SLOT_MISMATCH);
    }

    private static JwtAuthenticationToken patientAuth(Long patientId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(AppConstants.JwtClaims.PATIENT_ID, patientId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
    }

    private static JwtAuthenticationToken adminAuth() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "admin")
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static AppointmentDto toDto(Appointment entity) {
        return new AppointmentDto(
                entity.getId(),
                entity.getSlotId(),
                entity.getPatientId(),
                entity.getDoctorId(),
                entity.getDepartmentCode(),
                entity.getMode(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getStatus(),
                entity.getReason()
        );
    }
}
