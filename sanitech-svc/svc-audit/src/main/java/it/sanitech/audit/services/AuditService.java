package it.sanitech.audit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.audit.repositories.AuditEventRepository;
import it.sanitech.audit.repositories.entities.AuditEvent;
import it.sanitech.audit.repositories.spec.AuditEventSpecifications;
import it.sanitech.audit.services.dto.AuditEventCreateDto;
import it.sanitech.audit.services.dto.AuditEventDto;
import it.sanitech.audit.services.mapper.AuditEventMapper;
import it.sanitech.audit.utilities.AppConstants;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.core.DomainEventPublisher;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Service applicativo del bounded context "Audit".
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String AGGREGATE_TYPE = "AUDIT_EVENT";

    private final AuditEventRepository repository;
    private final AuditEventMapper mapper;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final MeterRegistry meterRegistry;

    @Transactional
    public AuditEventDto recordFromApi(AuditEventCreateDto dto, Authentication auth, String clientIp) {
        String actorId = auth != null ? auth.getName() : null;
        String actorType = resolveActorType(auth);
        String outcome = (dto.outcome() == null || dto.outcome().isBlank())
                ? AppConstants.Audit.OUTCOME_SUCCESS
                : dto.outcome().trim().toUpperCase();

        JsonNode details = dto.details() == null ? null : objectMapper.valueToTree(dto.details());

        AuditEvent saved = repository.save(AuditEvent.builder()
                .occurredAt(Instant.now())
                .source(AppConstants.Audit.SOURCE_API)
                .actorType(actorType)
                .actorId(actorId)
                .action(dto.action())
                .resourceType(dto.resourceType())
                .resourceId(dto.resourceId())
                .outcome(outcome)
                .ip(clientIp)
                .traceId(MDC.get("traceId"))
                .details(details)
                .build());

        meterRegistry.counter(AppConstants.Audit.METRIC_AUDIT_EVENTS_SAVED).increment();

        // Pubblicazione opzionale verso Kafka via Outbox (per pipeline analytics / SIEM).
        domainEventPublisher.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                "AUDIT_RECORDED",
                Map.of(
                        "auditId", saved.getId(),
                        "occurredAt", saved.getOccurredAt().toString(),
                        "source", saved.getSource(),
                        "actorType", saved.getActorType(),
                        "actorId", saved.getActorId(),
                        "action", saved.getAction(),
                        "resourceType", saved.getResourceType(),
                        "resourceId", saved.getResourceId(),
                        "outcome", saved.getOutcome(),
                        "traceId", saved.getTraceId()
                )
        );

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Bulkhead(name = "auditRead")
    public AuditEventDto getById(Long id) {
        AuditEvent evt = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Evento audit", id));
        return mapper.toDto(evt);
    }

    @Transactional(readOnly = true)
    @Bulkhead(name = "auditRead")
    public Page<AuditEventDto> search(
            String actorId,
            String action,
            String resourceType,
            String resourceId,
            String outcome,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        var spec = AuditEventSpecifications.actorIdEquals(actorId)
                .and(AuditEventSpecifications.actionEquals(action))
                .and(AuditEventSpecifications.resourceTypeEquals(resourceType))
                .and(AuditEventSpecifications.resourceIdEquals(resourceId))
                .and(AuditEventSpecifications.outcomeEquals(outcome))
                .and(AuditEventSpecifications.occurredFrom(from))
                .and(AuditEventSpecifications.occurredTo(to));

        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    private static String resolveActorType(Authentication auth) {
        if (auth == null) {
            return "ANONYMOUS";
        }
        boolean hasRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith(it.sanitech.commons.utilities.AppConstants.Security.ROLE_PREFIX));
        return hasRole ? "USER" : "SERVICE";
    }
}
