package it.sanitech.payments.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.payments.repositories.PaymentOrderRepository;
import it.sanitech.payments.repositories.entities.PaymentMethod;
import it.sanitech.payments.repositories.entities.PaymentOrder;
import it.sanitech.payments.repositories.entities.PaymentStatus;
import it.sanitech.payments.security.PaymentAccessGuard;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.create.PaymentCreateDto;
import it.sanitech.payments.services.dto.update.PaymentUpdateDto;
import it.sanitech.payments.services.mapper.PaymentOrderMapper;
import it.sanitech.payments.utilities.AppConstants;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class PaymentOrderServiceTest {

    @Test
    void createForCurrentPatientSetsStatusAndPublishesEvent() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        PaymentCreateDto dto = new PaymentCreateDto(10L, 1500L, "EUR", PaymentMethod.CARD, "Visit");
        PaymentOrder entity = PaymentOrder.builder()
                .appointmentId(10L)
                .amountCents(1500L)
                .currency("EUR")
                .method(PaymentMethod.CARD)
                .description("Visit")
                .build();

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder saved = invocation.getArgument(0);
            saved.setId(55L);
            saved.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            return saved;
        });
        when(mapper.toDto(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder saved = invocation.getArgument(0);
            return new PaymentOrderDto(
                    saved.getId(),
                    saved.getAppointmentId(),
                    saved.getPatientId(),
                    saved.getPatientEmail(),
                    saved.getPatientName(),
                    saved.getAmountCents(),
                    saved.getCurrency(),
                    saved.getMethod(),
                    saved.getProvider(),
                    saved.getProviderReference(),
                    saved.getStatus(),
                    saved.getDescription(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );
        });

        JwtAuthenticationToken auth = patientAuth(77L);

        PaymentOrderDto result = service.createForCurrentPatient(dto, "idem-1", auth);

        assertThat(result.status()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.patientId()).isEqualTo(77L);

        ArgumentCaptor<PaymentOrder> captor = ArgumentCaptor.forClass(PaymentOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("idem-1");

        verify(publisher).publish(eq(AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT), eq("55"), eq(AppConstants.Outbox.EVT_CREATED), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void listForCurrentUserReturnsEmptyWhenNoPid() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user")
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, java.util.List.of(new SimpleGrantedAuthority(AppConstants.Security.ROLE_PATIENT)));

        assertThat(service.listForCurrentUser(org.springframework.data.domain.PageRequest.of(0, 5), auth).getTotalElements())
                .isZero();
    }

    @Test
    void getByIdChecksAccess() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        PaymentOrder order = PaymentOrder.builder()
                .id(10L)
                .patientId(77L)
                .appointmentId(11L)
                .amountCents(1000)
                .currency("EUR")
                .method(PaymentMethod.CARD)
                .status(PaymentStatus.CREATED)
                .build();

        when(repository.findById(10L)).thenReturn(Optional.of(order));
        when(mapper.toDto(order)).thenReturn(new PaymentOrderDto(
                order.getId(),
                order.getAppointmentId(),
                order.getPatientId(),
                order.getPatientEmail(),
                order.getPatientName(),
                order.getAmountCents(),
                order.getCurrency(),
                order.getMethod(),
                order.getProvider(),
                order.getProviderReference(),
                order.getStatus(),
                order.getDescription(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        ));

        JwtAuthenticationToken auth = patientAuth(77L);

        PaymentOrderDto result = service.getById(10L, auth);

        assertThat(result.id()).isEqualTo(10L);
        verify(accessGuard).checkCanAccess(order, auth);
    }

    @Test
    void captureUpdatesStatusAndPublishesEvent() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        PaymentOrder order = PaymentOrder.builder()
                .id(22L)
                .status(PaymentStatus.CREATED)
                .build();

        when(repository.findById(22L)).thenReturn(Optional.of(order));
        when(repository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder saved = invocation.getArgument(0);
            return new PaymentOrderDto(
                    saved.getId(),
                    saved.getAppointmentId(),
                    saved.getPatientId(),
                    saved.getPatientEmail(),
                    saved.getPatientName(),
                    saved.getAmountCents(),
                    saved.getCurrency(),
                    saved.getMethod(),
                    saved.getProvider(),
                    saved.getProviderReference(),
                    saved.getStatus(),
                    saved.getDescription(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );
        });

        PaymentOrderDto result = service.capture(22L, null);

        assertThat(result.status()).isEqualTo(PaymentStatus.CAPTURED);
        verify(publisher).publish(eq(AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT), eq("22"), eq(AppConstants.Outbox.EVT_STATUS_CHANGED), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void refundRequiresCapturedStatus() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        PaymentOrder order = PaymentOrder.builder()
                .id(33L)
                .status(PaymentStatus.CREATED)
                .build();

        when(repository.findById(33L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.refund(33L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void adminPatchPublishesStatusChange() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        PaymentOrder order = PaymentOrder.builder()
                .id(44L)
                .status(PaymentStatus.CREATED)
                .build();

        when(repository.findById(44L)).thenReturn(Optional.of(order));
        when(repository.save(any(PaymentOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.doAnswer(invocation -> {
            PaymentUpdateDto update = invocation.getArgument(0);
            PaymentOrder target = invocation.getArgument(1);
            target.setStatus(update.status());
            target.setProviderReference(update.providerReference());
            return null;
        }).when(mapper).patch(any(PaymentUpdateDto.class), any(PaymentOrder.class));
        when(mapper.toDto(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder saved = invocation.getArgument(0);
            return new PaymentOrderDto(
                    saved.getId(),
                    saved.getAppointmentId(),
                    saved.getPatientId(),
                    saved.getPatientEmail(),
                    saved.getPatientName(),
                    saved.getAmountCents(),
                    saved.getCurrency(),
                    saved.getMethod(),
                    saved.getProvider(),
                    saved.getProviderReference(),
                    saved.getStatus(),
                    saved.getDescription(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );
        });

        PaymentUpdateDto updateDto = new PaymentUpdateDto(PaymentStatus.CAPTURED, "ref", "note");

        PaymentOrderDto result = service.adminPatch(44L, updateDto, null);

        assertThat(result.status()).isEqualTo(PaymentStatus.CAPTURED);
        verify(publisher).publish(eq(AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT), eq("44"), eq(AppConstants.Outbox.EVT_STATUS_CHANGED), any(), eq("audits.events"), (org.springframework.security.core.Authentication) any());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        PaymentOrderRepository repository = Mockito.mock(PaymentOrderRepository.class);
        PaymentOrderMapper mapper = Mockito.mock(PaymentOrderMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);
        PaymentAccessGuard accessGuard = Mockito.mock(PaymentAccessGuard.class);

        PaymentOrderService service = new PaymentOrderService(repository, mapper, publisher, accessGuard);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L, null))
                .isInstanceOf(NotFoundException.class);
    }

    private static JwtAuthenticationToken patientAuth(Long patientId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(AppConstants.Claims.PATIENT_ID, patientId)
                .build();
        return new JwtAuthenticationToken(jwt, java.util.List.of(new SimpleGrantedAuthority(AppConstants.Security.ROLE_PATIENT)));
    }
}
