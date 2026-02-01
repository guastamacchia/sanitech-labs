package it.sanitech.audit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.audit.repositories.AuditEventRepository;
import it.sanitech.audit.repositories.entities.AuditEvent;
import it.sanitech.audit.services.dto.AuditEventCreateDto;
import it.sanitech.audit.services.dto.AuditEventDto;
import it.sanitech.audit.services.mapper.AuditEventMapper;
import it.sanitech.audit.utilities.AppConstants;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.core.DomainEventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.slf4j.MDC;

class AuditServiceTest {

    @Test
    void recordFromApiSavesEventAndPublishesDomainEvent() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditEventMapper mapper = Mockito.mock(AuditEventMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        AuditService service = new AuditService(repository, mapper, objectMapper, publisher, meterRegistry);

        AuditEventCreateDto dto = new AuditEventCreateDto("LOGIN", "USER", "42", null, Map.of("ip", "127.0.0.1"));
        Authentication auth = new TestingAuthenticationToken("alice", "pwd", "ROLE_ADMIN");

        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> {
            AuditEvent saved = invocation.getArgument(0);
            return saved.toBuilder().id(101L).build();
        });
        when(mapper.toDto(any(AuditEvent.class))).thenAnswer(invocation -> {
            AuditEvent saved = invocation.getArgument(0);
            return new AuditEventDto(
                    saved.getId(),
                    saved.getOccurredAt(),
                    saved.getSource(),
                    saved.getActorType(),
                    saved.getActorId(),
                    saved.getAction(),
                    saved.getResourceType(),
                    saved.getResourceId(),
                    saved.getOutcome(),
                    saved.getIp(),
                    saved.getTraceId(),
                    saved.getDetails()
            );
        });

        MDC.put("traceId", "trace-1");
        AuditEventDto result;
        try {
            result = service.recordFromApi(dto, auth, "10.0.0.1");
        } finally {
            MDC.remove("traceId");
        }

        assertThat(result.id()).isEqualTo(101L);
        assertThat(result.outcome()).isEqualTo(AppConstants.Audit.OUTCOME_SUCCESS);
        assertThat(result.actorType()).isEqualTo("USER");
        assertThat(meterRegistry.counter(AppConstants.Audit.METRIC_AUDIT_EVENTS_SAVED).count()).isEqualTo(1.0);

        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(eventCaptor.capture());
        AuditEvent saved = eventCaptor.getValue();
        assertThat(saved.getSource()).isEqualTo(AppConstants.Audit.SOURCE_API);
        assertThat(saved.getActorId()).isEqualTo("alice");
        assertThat(saved.getIp()).isEqualTo("10.0.0.1");
        assertThat(saved.getOccurredAt()).isNotNull();

        verify(publisher).publish(eq("AUDIT_EVENT"), anyString(), eq("AUDIT_RECORDED"), any(Map.class));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditEventMapper mapper = Mockito.mock(AuditEventMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        AuditService service = new AuditService(repository, mapper, objectMapper, publisher, meterRegistry);

        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdReturnsDto() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditEventMapper mapper = Mockito.mock(AuditEventMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        AuditService service = new AuditService(repository, mapper, objectMapper, publisher, meterRegistry);

        AuditEvent event = AuditEvent.builder()
                .id(3L)
                .occurredAt(Instant.parse("2024-01-01T00:00:00Z"))
                .source(AppConstants.Audit.SOURCE_API)
                .actorType("USER")
                .actorId("bob")
                .action("UPDATE")
                .outcome(AppConstants.Audit.OUTCOME_SUCCESS)
                .ip("10.0.0.2")
                .build();

        when(repository.findById(3L)).thenReturn(Optional.of(event));
        when(mapper.toDto(event)).thenReturn(new AuditEventDto(
                event.getId(),
                event.getOccurredAt(),
                event.getSource(),
                event.getActorType(),
                event.getActorId(),
                event.getAction(),
                event.getResourceType(),
                event.getResourceId(),
                event.getOutcome(),
                event.getIp(),
                event.getTraceId(),
                event.getDetails()
        ));

        AuditEventDto result = service.getById(3L);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.action()).isEqualTo("UPDATE");
    }

    @Test
    void searchDelegatesToRepositoryAndMapsResults() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        AuditEventMapper mapper = Mockito.mock(AuditEventMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        AuditService service = new AuditService(repository, mapper, objectMapper, publisher, meterRegistry);

        AuditEvent event = AuditEvent.builder()
                .id(5L)
                .occurredAt(Instant.parse("2024-01-02T00:00:00Z"))
                .source(AppConstants.Audit.SOURCE_API)
                .actorType("SERVICE")
                .actorId("svc-directory")
                .action("SYNC")
                .outcome(AppConstants.Audit.OUTCOME_SUCCESS)
                .build();

        Page<AuditEvent> page = new PageImpl<>(List.of(event), PageRequest.of(0, 20), 1);
        when(repository.findAll(Mockito.<Specification<AuditEvent>>any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(event)).thenReturn(new AuditEventDto(
                event.getId(),
                event.getOccurredAt(),
                event.getSource(),
                event.getActorType(),
                event.getActorId(),
                event.getAction(),
                event.getResourceType(),
                event.getResourceId(),
                event.getOutcome(),
                event.getIp(),
                event.getTraceId(),
                event.getDetails()
        ));

        Page<AuditEventDto> result = service.search(
                "svc-directory",
                "SYNC",
                null,
                null,
                AppConstants.Audit.OUTCOME_SUCCESS,
                null,
                null,
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(Mockito.<Specification<AuditEvent>>any(), any(Pageable.class));
    }
}
