package it.sanitech.televisit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.televisit.repositories.TelevisitSessionRepository;
import it.sanitech.televisit.repositories.entities.TelevisitSession;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.services.dto.create.TelevisitCreateDto;
import it.sanitech.televisit.services.livekit.LiveKitRoomService;
import it.sanitech.televisit.services.livekit.LiveKitTokenService;
import it.sanitech.televisit.services.mapper.TelevisitMapper;
import it.sanitech.televisit.utilities.AppConstants;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class TelevisitServiceTest {

    @Test
    void createPublishesEventAndEnsuresRoom() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitCreateDto dto = new TelevisitCreateDto(
                "doc-subject",
                "patient-subject",
                "cardio",
                OffsetDateTime.parse("2024-01-01T10:00:00Z")
        );
        when(repo.save(any(TelevisitSession.class))).thenAnswer(invocation -> {
            TelevisitSession saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(mapper.toDto(any(TelevisitSession.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        TelevisitDto result = service.create(dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(TelevisitStatus.CREATED);

        ArgumentCaptor<TelevisitSession> captor = ArgumentCaptor.forClass(TelevisitSession.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getRoomName()).startsWith("tv-");

        verify(roomService).ensureRoomExists(eq(captor.getValue().getRoomName()));
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION), eq("10"), eq(AppConstants.Outbox.EventType.CREATED), any(), eq("audits.events"));
    }

    @Test
    void searchReturnsPage() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitSession entity = TelevisitSession.builder()
                .id(12L)
                .roomName("tv-1")
                .department("CARDIO")
                .doctorSubject("doc-subject")
                .patientSubject("patient-subject")
                .scheduledAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .status(TelevisitStatus.CREATED)
                .build();
        when(repo.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 1));
        when(mapper.toDto(any(TelevisitSession.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        Page<TelevisitDto> page = service.search("CARDIO", TelevisitStatus.CREATED, null, null, PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(12L);
    }

    @Test
    void issueDoctorTokenChecksSubject() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitSession entity = TelevisitSession.builder()
                .id(20L)
                .roomName("tv-room")
                .department("CARDIO")
                .doctorSubject("doc-subject")
                .patientSubject("patient-subject")
                .scheduledAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .status(TelevisitStatus.CREATED)
                .build();
        when(repo.findById(20L)).thenReturn(Optional.of(entity));
        LiveKitTokenDto tokenDto = new LiveKitTokenDto("tv-room", "https://livekit", "token", 3600);
        when(tokenService.createRoomJoinToken("tv-room", "doc-subject", "doctor")).thenReturn(tokenDto);

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");
        LiveKitTokenDto result = service.issueDoctorToken(20L, auth);

        assertThat(result.roomName()).isEqualTo("tv-room");
        verify(deptGuard).checkCanManage(eq("CARDIO"), eq(auth));
    }

    @Test
    void startPublishesEvent() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitSession entity = TelevisitSession.builder()
                .id(30L)
                .roomName("tv-room")
                .department("CARDIO")
                .doctorSubject("doc-subject")
                .patientSubject("patient-subject")
                .scheduledAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .status(TelevisitStatus.CREATED)
                .build();
        when(repo.findById(30L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(any(TelevisitSession.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");
        TelevisitDto result = service.start(30L, auth);

        assertThat(result.status()).isEqualTo(TelevisitStatus.ACTIVE);
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION), eq("30"), eq(AppConstants.Outbox.EventType.STARTED), any(), eq("audits.events"));
    }

    @Test
    void endThrowsWhenNotActive() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitSession entity = TelevisitSession.builder()
                .id(40L)
                .roomName("tv-room")
                .department("CARDIO")
                .doctorSubject("doc-subject")
                .patientSubject("patient-subject")
                .scheduledAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .status(TelevisitStatus.CREATED)
                .build();
        when(repo.findById(40L)).thenReturn(Optional.of(entity));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");
        assertThatThrownBy(() -> service.end(40L, auth))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelPublishesEvent() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        TelevisitSession entity = TelevisitSession.builder()
                .id(50L)
                .roomName("tv-room")
                .department("CARDIO")
                .doctorSubject("doc-subject")
                .patientSubject("patient-subject")
                .scheduledAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"))
                .status(TelevisitStatus.CREATED)
                .build();
        when(repo.findById(50L)).thenReturn(Optional.of(entity));
        when(mapper.toDto(any(TelevisitSession.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");
        TelevisitDto result = service.cancel(50L, auth);

        assertThat(result.status()).isEqualTo(TelevisitStatus.CANCELED);
        verify(events).publish(eq(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION), eq("50"), eq(AppConstants.Outbox.EventType.CANCELED), any(), eq("audits.events"));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        TelevisitSessionRepository repo = Mockito.mock(TelevisitSessionRepository.class);
        TelevisitMapper mapper = Mockito.mock(TelevisitMapper.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        LiveKitRoomService roomService = Mockito.mock(LiveKitRoomService.class);
        LiveKitTokenService tokenService = Mockito.mock(LiveKitTokenService.class);

        TelevisitService service = new TelevisitService(repo, mapper, events, deptGuard, roomService, tokenService);

        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    private static TelevisitDto toDto(TelevisitSession entity) {
        return new TelevisitDto(
                entity.getId(),
                entity.getRoomName(),
                entity.getDepartment(),
                entity.getDoctorSubject(),
                entity.getPatientSubject(),
                entity.getScheduledAt(),
                entity.getStatus()
        );
    }
}
