package it.sanitech.scheduling.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.scheduling.repositories.SlotRepository;
import it.sanitech.scheduling.repositories.entities.Slot;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import it.sanitech.scheduling.services.mapper.SlotMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SlotServiceTest {

    @Test
    void createSlotPublishesEvent() {
        SlotRepository repository = Mockito.mock(SlotRepository.class);
        SlotMapper mapper = Mockito.mock(SlotMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        SlotService service = new SlotService(repository, mapper, deptGuard, events);

        SlotCreateDto dto = new SlotCreateDto(
                22L,
                "CARDIO",
                VisitMode.IN_PERSON,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:30:00Z")
        );
        Slot entity = Slot.builder()
                .doctorId(22L)
                .departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON)
                .startAt(dto.startAt())
                .endAt(dto.endAt())
                .status(SlotStatus.AVAILABLE)
                .build();
        when(mapper.fromCreateDto(dto)).thenReturn(entity);
        when(repository.save(any(Slot.class))).thenAnswer(invocation -> {
            Slot saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });
        when(mapper.toDto(any(Slot.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        JwtAuthenticationToken auth = adminAuth();
        SlotDto result = service.createSlot(dto, auth);

        assertThat(result.id()).isEqualTo(11L);
        verify(deptGuard).checkCanManage(eq("CARDIO"), eq(auth));
        verify(events).publish(eq("SLOT"), eq("11"), eq("SLOT_CREATED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void createSlotsBulkRejectsEmptyList() {
        SlotRepository repository = Mockito.mock(SlotRepository.class);
        SlotMapper mapper = Mockito.mock(SlotMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        SlotService service = new SlotService(repository, mapper, deptGuard, events);

        assertThatThrownBy(() -> service.createSlotsBulk(List.of(), adminAuth()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(AppConstants.ErrorMessage.MSG_EMPTY_SLOT_LIST);
    }

    @Test
    void cancelSlotUpdatesStatus() {
        SlotRepository repository = Mockito.mock(SlotRepository.class);
        SlotMapper mapper = Mockito.mock(SlotMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        SlotService service = new SlotService(repository, mapper, deptGuard, events);

        Slot entity = Slot.builder()
                .id(20L)
                .departmentCode("CARDIO")
                .status(SlotStatus.AVAILABLE)
                .build();
        when(repository.findById(20L)).thenReturn(Optional.of(entity));
        when(repository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JwtAuthenticationToken auth = adminAuth();
        service.cancelSlot(20L, auth);

        assertThat(entity.getStatus()).isEqualTo(SlotStatus.CANCELLED);
        verify(deptGuard).checkCanManage(eq("CARDIO"), eq(auth));
        verify(events).publish(eq("SLOT"), eq("20"), eq("SLOT_CANCELLED"), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void cancelSlotThrowsWhenMissing() {
        SlotRepository repository = Mockito.mock(SlotRepository.class);
        SlotMapper mapper = Mockito.mock(SlotMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        SlotService service = new SlotService(repository, mapper, deptGuard, events);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelSlot(99L, adminAuth()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchAvailableSlotsReturnsPage() {
        SlotRepository repository = Mockito.mock(SlotRepository.class);
        SlotMapper mapper = Mockito.mock(SlotMapper.class);
        DeptGuard deptGuard = Mockito.mock(DeptGuard.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);

        SlotService service = new SlotService(repository, mapper, deptGuard, events);

        Slot entity = Slot.builder()
                .id(30L)
                .doctorId(22L)
                .departmentCode("CARDIO")
                .mode(VisitMode.IN_PERSON)
                .startAt(Instant.parse("2024-01-01T10:00:00Z"))
                .endAt(Instant.parse("2024-01-01T10:30:00Z"))
                .status(SlotStatus.AVAILABLE)
                .build();
        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 1), 1));
        when(mapper.toDto(any(Slot.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        Page<SlotDto> page = service.searchAvailableSlots(22L, "CARDIO", VisitMode.IN_PERSON, null, null, 0, 1, new String[]{"startAt,asc"});

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).id()).isEqualTo(30L);
    }

    private static JwtAuthenticationToken adminAuth() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "admin")
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private static SlotDto toDto(Slot entity) {
        return new SlotDto(
                entity.getId(),
                entity.getDoctorId(),
                entity.getDepartmentCode(),
                entity.getMode(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getStatus()
        );
    }
}
