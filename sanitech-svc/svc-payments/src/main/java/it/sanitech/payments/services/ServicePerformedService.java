package it.sanitech.payments.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.payments.repositories.ServicePerformedRepository;
import it.sanitech.payments.repositories.entities.ServicePerformed;
import it.sanitech.payments.repositories.entities.ServicePerformedStatus;
import it.sanitech.payments.services.dto.ServicePerformedDto;
import it.sanitech.payments.services.dto.ServicePerformedStatsDto;
import it.sanitech.payments.services.dto.update.ServicePerformedUpdateDto;
import it.sanitech.payments.services.mapper.ServicePerformedMapper;
import it.sanitech.payments.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service applicativo per la gestione delle prestazioni sanitarie.
 */
@Service
@RequiredArgsConstructor
public class ServicePerformedService {

    private final ServicePerformedRepository repository;
    private final ServicePerformedMapper mapper;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Lista tutte le prestazioni con filtri opzionali.
     */
    @Bulkhead(name = "paymentsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<ServicePerformedDto> list(ServicePerformedStatus status, Pageable pageable) {
        Specification<ServicePerformed> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return repository.findAll(spec, pageable).map(mapper::toDto);
    }

    /**
     * Ottiene una prestazione per ID.
     */
    @Transactional(readOnly = true)
    public ServicePerformedDto getById(long id) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));
        return mapper.toDto(service);
    }

    /**
     * Aggiorna parzialmente una prestazione (admin).
     */
    @Transactional
    public ServicePerformedDto patch(long id, ServicePerformedUpdateDto dto, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        mapper.patch(dto, service);

        ServicePerformed saved = repository.save(service);

        // Pubblica evento di modifica
        publishStatusChanged(saved, auth);

        return mapper.toDto(saved);
    }

    /**
     * Marca una prestazione come gratuita.
     */
    @Transactional
    public ServicePerformedDto markAsFree(long id, String reason, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        if (service.getStatus() == ServicePerformedStatus.PAID) {
            throw new IllegalArgumentException("Impossibile convertire in gratuita: prestazione già pagata.");
        }

        service.markFree(reason);
        ServicePerformed saved = repository.save(service);

        publishStatusChanged(saved, auth);

        return mapper.toDto(saved);
    }

    /**
     * Marca una prestazione come pagata.
     */
    @Transactional
    public ServicePerformedDto markAsPaid(long id, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        if (service.getStatus() == ServicePerformedStatus.PAID) {
            return mapper.toDto(service);
        }

        if (service.getStatus() == ServicePerformedStatus.CANCELLED) {
            throw new IllegalArgumentException("Impossibile segnare come pagata: prestazione annullata.");
        }

        service.markPaid();
        ServicePerformed saved = repository.save(service);

        publishStatusChanged(saved, auth);

        return mapper.toDto(saved);
    }

    /**
     * Annulla una prestazione (soft delete).
     */
    @Transactional
    public ServicePerformedDto cancel(long id, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        if (service.getStatus() == ServicePerformedStatus.PAID) {
            throw new IllegalArgumentException("Impossibile annullare: prestazione già pagata.");
        }

        service.markCancelled();
        ServicePerformed saved = repository.save(service);

        publishStatusChanged(saved, auth);

        return mapper.toDto(saved);
    }

    /**
     * Elimina definitivamente una prestazione.
     */
    @Transactional
    public void delete(long id, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        repository.delete(service);

        // Pubblica evento di eliminazione
        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_SERVICE,
                String.valueOf(id),
                AppConstants.Outbox.EVT_SERVICE_DELETED,
                Map.of("serviceId", id),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    /**
     * Invia un sollecito di pagamento al paziente.
     */
    @Transactional
    public ServicePerformedDto sendReminder(long id, Authentication auth) {
        ServicePerformed service = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ServicePerformed", id));

        if (service.getPatientEmail() == null || service.getPatientEmail().isBlank()) {
            throw new IllegalArgumentException("Impossibile inviare sollecito: email paziente mancante.");
        }

        if (service.getStatus() != ServicePerformedStatus.PENDING) {
            throw new IllegalArgumentException("Sollecito consentito solo per prestazioni in attesa di pagamento.");
        }

        service.recordReminderSent();
        ServicePerformed saved = repository.save(service);

        // Pubblica evento per invio email
        Map<String, Object> payload = new HashMap<>();
        payload.put("recipientType", "PATIENT");
        payload.put("recipientId", service.getPatientEmail());
        payload.put("email", service.getPatientEmail());
        payload.put("patientName", service.getPatientName());
        payload.put("serviceId", service.getId());
        payload.put("amountCents", service.getAmountCents());
        payload.put("currency", service.getCurrency());
        payload.put("description", service.getDescription());

        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_SERVICE,
                String.valueOf(service.getId()),
                AppConstants.Outbox.EVT_SERVICE_REMINDER_REQUESTED,
                payload,
                AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Invia solleciti multipli.
     */
    @Transactional
    public void sendBulkReminders(List<Long> ids, Authentication auth) {
        List<ServicePerformed> services = repository.findByIdIn(ids);

        for (ServicePerformed service : services) {
            if (service.getStatus() == ServicePerformedStatus.PENDING
                    && service.getPatientEmail() != null
                    && !service.getPatientEmail().isBlank()) {

                service.recordReminderSent();
                repository.save(service);

                Map<String, Object> payload = new HashMap<>();
                payload.put("recipientType", "PATIENT");
                payload.put("recipientId", service.getPatientEmail());
                payload.put("email", service.getPatientEmail());
                payload.put("patientName", service.getPatientName());
                payload.put("serviceId", service.getId());
                payload.put("amountCents", service.getAmountCents());
                payload.put("currency", service.getCurrency());
                payload.put("description", service.getDescription());

                domainEventPublisher.publish(
                        AppConstants.Outbox.AGGREGATE_TYPE_SERVICE,
                        String.valueOf(service.getId()),
                        AppConstants.Outbox.EVT_SERVICE_REMINDER_REQUESTED,
                        payload,
                        AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS,
                        auth
                );
            }
        }
    }

    /**
     * Calcola le statistiche delle prestazioni dell'ultimo mese.
     */
    @Transactional(readOnly = true)
    public ServicePerformedStatsDto getStats() {
        Instant oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        long total = repository.count();
        long pending = repository.countByStatus(ServicePerformedStatus.PENDING);
        long paid = repository.countByStatus(ServicePerformedStatus.PAID);

        // Calcola pagate entro 7 giorni dalla prestazione
        // Approssimazione: contiamo le PAID con performedAt negli ultimi 7 giorni
        long paidWithin7Days = repository.countByStatusAndPerformedAtAfter(ServicePerformedStatus.PAID, sevenDaysAgo);

        // Pagate con sollecito (reminderCount > 0)
        // Non abbiamo una query specifica, usiamo 0 per ora
        long paidWithReminder = 0;

        double percentWithin7Days = total > 0 ? (paidWithin7Days * 100.0 / total) : 0;
        double percentWithReminder = total > 0 ? (paidWithReminder * 100.0 / total) : 0;
        double percentPending = total > 0 ? (pending * 100.0 / total) : 0;

        return new ServicePerformedStatsDto(
                total,
                paidWithin7Days,
                paidWithReminder,
                pending,
                Math.round(percentWithin7Days * 10) / 10.0,
                Math.round(percentWithReminder * 10) / 10.0,
                Math.round(percentPending * 10) / 10.0,
                total
        );
    }

    private void publishStatusChanged(ServicePerformed service, Authentication auth) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceId", service.getId());
        payload.put("status", service.getStatus().name());
        payload.put("amountCents", service.getAmountCents());

        domainEventPublisher.publish(
                AppConstants.Outbox.AGGREGATE_TYPE_SERVICE,
                String.valueOf(service.getId()),
                AppConstants.Outbox.EVT_SERVICE_STATUS_CHANGED,
                payload,
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }
}
