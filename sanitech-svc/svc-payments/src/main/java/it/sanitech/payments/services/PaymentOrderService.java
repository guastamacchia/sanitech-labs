package it.sanitech.payments.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.payments.repositories.PaymentOrderRepository;
import it.sanitech.payments.repositories.entities.PaymentOrder;
import it.sanitech.payments.repositories.entities.PaymentStatus;
import it.sanitech.payments.security.AuthClaims;
import it.sanitech.payments.security.PaymentAccessGuard;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.create.PaymentAdminCreateDto;
import it.sanitech.payments.services.dto.create.PaymentCreateDto;
import it.sanitech.payments.services.dto.update.PaymentUpdateDto;
import it.sanitech.payments.services.mapper.PaymentOrderMapper;
import it.sanitech.payments.utilities.AppConstants;
import it.sanitech.outbox.core.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service applicativo per la gestione degli ordini di pagamento.
 */
@Service
@RequiredArgsConstructor
public class PaymentOrderService {

    private final PaymentOrderRepository repository;
    private final PaymentOrderMapper mapper;
    private final DomainEventPublisher domainEventPublisher;
    private final PaymentAccessGuard accessGuard;

    /**
     * Crea un ordine di pagamento per il paziente corrente.
     *
     * <p>
     * Supporta idempotency tramite header {@code X-Idempotency-Key} (se presente).
     * </p>
     */
    @Transactional
    public PaymentOrderDto createForCurrentPatient(PaymentCreateDto dto, String idempotencyKey, Authentication auth) {
        Long pid = AuthClaims.patientId(auth)
                .orElseThrow(() -> new IllegalArgumentException("Claim pid mancante nel token del paziente."));

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<PaymentOrder> existing = repository.findByIdempotencyKeyIgnoreCase(idempotencyKey);
            if (existing.isPresent()) {
                accessGuard.checkCanAccess(existing.get(), auth);
                return mapper.toDto(existing.get());
            }
        }

        PaymentOrder entity = mapper.toEntity(dto);
        entity.setPatientId(pid);
        entity.setStatus(PaymentStatus.CREATED);
        entity.setIdempotencyKey(normalizeIdempotencyKey(idempotencyKey));
        entity.setCreatedBy(auth != null ? auth.getName() : "system");

        PaymentOrder saved = repository.save(entity);

        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_CREATED,
                Map.of(
                        "paymentId", saved.getId(),
                        "appointmentId", saved.getAppointmentId(),
                        "patientId", saved.getPatientId(),
                        "amountCents", saved.getAmountCents(),
                        "currency", saved.getCurrency(),
                        "method", saved.getMethod().name(),
                        "status", saved.getStatus().name()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Crea un ordine di pagamento lato amministrazione (backoffice).
     */
    @Transactional
    public PaymentOrderDto adminCreate(PaymentAdminCreateDto dto, String idempotencyKey, Authentication auth) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<PaymentOrder> existing = repository.findByIdempotencyKeyIgnoreCase(idempotencyKey);
            if (existing.isPresent()) {
                return mapper.toDto(existing.get());
            }
        }

        PaymentOrder entity = mapper.toEntity(dto);
        entity.setStatus(PaymentStatus.CREATED);
        entity.setIdempotencyKey(normalizeIdempotencyKey(idempotencyKey));
        entity.setCreatedBy(auth != null ? auth.getName() : "system");

        PaymentOrder saved = repository.save(entity);

        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_CREATED,
                Map.of(
                        "paymentId", saved.getId(),
                        "appointmentId", saved.getAppointmentId(),
                        "patientId", saved.getPatientId(),
                        "amountCents", saved.getAmountCents(),
                        "currency", saved.getCurrency(),
                        "method", saved.getMethod().name(),
                        "status", saved.getStatus().name()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Lista pagamenti per utente corrente:
     * - ADMIN: tutti
     * - PATIENT: solo i propri
     */
    @Bulkhead(name = "paymentsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<PaymentOrderDto> listForCurrentUser(Pageable pageable, Authentication auth) {
        boolean isAdmin = hasAuthority(auth, AppConstants.Security.ROLE_ADMIN);
        if (isAdmin) {
            return repository.findAll(pageable).map(mapper::toDto);
        }

        Long pid = AuthClaims.patientId(auth).orElse(null);
        if (pid == null) {
            return Page.empty(pageable);
        }
        return repository.findByPatientId(pid, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PaymentOrderDto getById(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));
        accessGuard.checkCanAccess(order, auth);
        return mapper.toDto(order);
    }

    /**
     * Aggiornamento parziale (admin).
     */
    @Transactional
    public PaymentOrderDto adminPatch(long id, PaymentUpdateDto dto, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));

        mapper.patch(dto, order);

        // Se viene patchato lo status, pubblichiamo un evento di change
        if (dto.status() != null) {
            publishStatusChanged(order, auth);
        }

        return mapper.toDto(repository.save(order));
    }

    /**
     * Cattura un pagamento verificando che appartenga al paziente corrente.
     */
    @Transactional
    public PaymentOrderDto captureForPatient(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));
        accessGuard.checkCanAccess(order, auth);
        return capture(id, auth);
    }

    @Transactional
    public PaymentOrderDto capture(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));

        if (order.getStatus() == PaymentStatus.CAPTURED) {
            return mapper.toDto(order);
        }
        if (order.getStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("Impossibile catturare un pagamento cancellato.");
        }
        if (order.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Impossibile catturare un pagamento rimborsato.");
        }

        order.markCaptured();
        PaymentOrder saved = repository.save(order);

        publishStatusChanged(saved, auth);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentOrderDto fail(long id, String providerReference, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));
        order.markFailed(providerReference);

        PaymentOrder saved = repository.save(order);
        publishStatusChanged(saved, auth);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentOrderDto cancel(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));
        order.markCancelled();

        PaymentOrder saved = repository.save(order);
        publishStatusChanged(saved, auth);
        return mapper.toDto(saved);
    }

    @Transactional
    public PaymentOrderDto refund(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id).orElseThrow(() -> NotFoundException.of("PaymentOrder", id));
        if (order.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalArgumentException("Un rimborso è consentito solo per pagamenti CAPTURED.");
        }

        order.markRefunded();
        PaymentOrder saved = repository.save(order);
        publishStatusChanged(saved, auth);
        return mapper.toDto(saved);
    }

    /**
     * Aggiornamento stato da webhook provider (identificazione per provider+reference).
     * Nota: auth è null poiché proviene da webhook esterno (sistema).
     */
    @Transactional
    public PaymentOrderDto updateFromWebhook(String provider, String providerReference, PaymentStatus status) {
        PaymentOrder order = repository.findByProviderIgnoreCaseAndProviderReferenceIgnoreCase(provider, providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento non trovato per provider/reference."));

        if (status != null) {
            order.setStatus(status);
        }

        PaymentOrder saved = repository.save(order);
        publishStatusChanged(saved, null); // Webhook esterno, attore = SYSTEM
        return mapper.toDto(saved);
    }

    /**
     * Invia un sollecito di pagamento al paziente.
     *
     * <p>
     * Pubblica un evento {@code PAYMENT_REMINDER_REQUESTED} sul topic notifiche
     * che verrà consumato da svc-notifications per l'invio dell'email.
     * </p>
     */
    @Transactional
    public PaymentOrderDto sendReminder(long id, Authentication auth) {
        PaymentOrder order = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("PaymentOrder", id));

        if (order.getPatientEmail() == null || order.getPatientEmail().isBlank()) {
            throw new IllegalArgumentException("Impossibile inviare sollecito: email paziente mancante.");
        }

        if (order.getStatus() == PaymentStatus.CAPTURED) {
            throw new IllegalArgumentException("Impossibile inviare sollecito: pagamento già completato.");
        }

        if (order.getStatus() == PaymentStatus.CANCELLED || order.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Impossibile inviare sollecito: pagamento annullato o rimborsato.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("recipientType", "PATIENT");
        payload.put("recipientId", order.getPatientEmail());
        payload.put("email", order.getPatientEmail());
        payload.put("patientName", order.getPatientName());
        payload.put("paymentId", order.getId());
        payload.put("amountCents", order.getAmountCents());
        payload.put("currency", order.getCurrency());
        payload.put("description", order.getDescription());

        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT,
                String.valueOf(order.getId()),
                AppConstants.Outbox.EVT_REMINDER_REQUESTED,
                payload,
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                auth
        );

        return mapper.toDto(order);
    }

    private void publishStatusChanged(PaymentOrder order, Authentication auth) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", order.getId());
        payload.put("status", order.getStatus().name());
        if (order.getProvider() != null) {
            payload.put("provider", order.getProvider());
        }
        if (order.getProviderReference() != null) {
            payload.put("providerReference", order.getProviderReference());
        }
        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_PAYMENT,
                String.valueOf(order.getId()),
                AppConstants.Outbox.EVT_STATUS_CHANGED,
                payload,
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) return null;
        String trimmed = idempotencyKey.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
