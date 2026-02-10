package it.sanitech.prescribing.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.prescribing.integrations.consents.ConsentClient;
import it.sanitech.prescribing.repositories.PrescriptionRepository;
import it.sanitech.prescribing.repositories.entities.Prescription;
import it.sanitech.prescribing.repositories.entities.PrescriptionItem;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
import it.sanitech.prescribing.security.JwtClaimUtils;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionCreateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionPatchDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionUpdateDto;
import it.sanitech.prescribing.services.mappers.PrescriptionMapper;
import it.sanitech.prescribing.utilities.AppConstants;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Service applicativo per la gestione delle prescrizioni.
 */
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptions;
    private final PrescriptionMapper mapper;
    private final DomainEventPublisher events;
    private final ConsentClient consentClient;
    private final DeptGuard deptGuard;

    /**
     * Crea una prescrizione per un paziente.
     *
     * <p>
     * Regole applicative:
     * <ul>
     *   <li>il medico è determinato dal token (claim {@code did});</li>
     *   <li>il reparto deve essere tra quelli autorizzati (authority {@code DEPT_*});</li>
     *   <li>è richiesto un consenso {@code PRESCRIPTIONS} in {@code svc-consents}.</li>
     * </ul>
     * </p>
     */
    @Transactional
    public PrescriptionDto create(PrescriptionCreateDto dto, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        deptGuard.checkCanManage(dto.departmentCode(), auth);

        consentClient.assertPrescriptionConsent(dto.patientId(), doctorId, auth);

        Prescription entity = mapper.toEntity(dto);
        entity.setDoctorId(doctorId);

        // Per semplicità emettiamo subito la prescrizione; in caso d'uso "bozze" basta non chiamare markIssued().
        entity.markIssued();

        List<PrescriptionItem> items = dto.items().stream().map(mapper::toEntity).toList();
        entity.replaceItems(items);

        Prescription saved = prescriptions.save(entity);

        events.publish(
                AppConstants.Outbox.AGGREGATE_PRESCRIPTION,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_PRESCRIPTION_CREATED,
                new PrescriptionEventPayload(saved),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Recupera una prescrizione dal punto di vista del paziente (solo le proprie).
     */
    @Transactional(readOnly = true)
    public PrescriptionDto getMine(Long prescriptionId, Authentication auth) {
        Long patientId = JwtClaimUtils.requirePatientId(auth);
        Prescription p = prescriptions.findByIdAndPatientId(prescriptionId, patientId)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", prescriptionId));
        return mapper.toDto(p);
    }

    /**
     * Stati visibili al paziente: le bozze (DRAFT) non devono essere esposte.
     */
    private static final Set<PrescriptionStatus> PATIENT_VISIBLE_STATUSES =
            Set.of(PrescriptionStatus.ISSUED, PrescriptionStatus.CANCELLED);

    /**
     * Lista prescrizioni del paziente autenticato.
     * Le prescrizioni DRAFT non sono visibili (sono bozze del medico).
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "prescribingRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<PrescriptionDto> listMine(PrescriptionStatus status, Pageable pageable, Authentication auth) {
        Long patientId = JwtClaimUtils.requirePatientId(auth);
        Page<Prescription> page;
        if (status != null) {
            page = PATIENT_VISIBLE_STATUSES.contains(status)
                    ? prescriptions.findByPatientIdAndStatus(patientId, status, pageable)
                    : Page.empty(pageable);
        } else {
            page = prescriptions.findByPatientIdAndStatusIn(patientId, PATIENT_VISIBLE_STATUSES, pageable);
        }

        return page.map(mapper::toDto);
    }

    /**
     * Recupera una prescrizione dal punto di vista del medico.
     *
     * <p>
     * Viene verificato il consenso prescrittivo del paziente verso il medico corrente.
     * </p>
     */
    @Transactional(readOnly = true)
    public PrescriptionDto getForDoctor(Long prescriptionId, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        Prescription p = prescriptions.findDetailedById(prescriptionId)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", prescriptionId));

        deptGuard.checkCanManage(p.getDepartmentCode(), auth);
        consentClient.assertPrescriptionConsent(p.getPatientId(), doctorId, auth);

        return mapper.toDto(p);
    }

    /**
     * Lista le prescrizioni di un paziente in un dato reparto.
     *
     * <p>
     * L'uso del filtro {@code departmentCode} evita di restituire dati di reparti non autorizzati.
     * </p>
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "prescribingRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<PrescriptionDto> listForDoctor(Long patientId, String departmentCode, Pageable pageable, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        deptGuard.checkCanManage(departmentCode, auth);

        consentClient.assertPrescriptionConsent(patientId, doctorId, auth);

        return prescriptions
                .findByPatientIdAndDepartmentCodeIgnoreCase(patientId, departmentCode, pageable)
                .map(mapper::toDto);
    }

    /**
     * Aggiorna una prescrizione sostituendo le righe (operazione tipo PUT).
     */
    @Transactional
    public PrescriptionDto update(Long prescriptionId, PrescriptionUpdateDto dto, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        Prescription p = prescriptions.findDetailedById(prescriptionId)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", prescriptionId));

        requireMutableStatus(p);
        deptGuard.checkCanManage(p.getDepartmentCode(), auth);
        consentClient.assertPrescriptionConsent(p.getPatientId(), doctorId, auth);

        p.setNotes(dto.notes());
        p.replaceItems(dto.items().stream().map(mapper::toEntity).toList());

        Prescription saved = prescriptions.save(p);

        events.publish(
                AppConstants.Outbox.AGGREGATE_PRESCRIPTION,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_PRESCRIPTION_UPDATED,
                new PrescriptionEventPayload(saved),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Patch parziale della prescrizione (attualmente solo note).
     */
    @Transactional
    public PrescriptionDto patch(Long prescriptionId, PrescriptionPatchDto dto, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        Prescription p = prescriptions.findDetailedById(prescriptionId)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", prescriptionId));

        requireMutableStatus(p);
        deptGuard.checkCanManage(p.getDepartmentCode(), auth);
        consentClient.assertPrescriptionConsent(p.getPatientId(), doctorId, auth);

        mapper.patch(p, dto);

        Prescription saved = prescriptions.save(p);

        events.publish(
                AppConstants.Outbox.AGGREGATE_PRESCRIPTION,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_PRESCRIPTION_UPDATED,
                new PrescriptionEventPayload(saved),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(saved);
    }

    /**
     * Annulla una prescrizione.
     *
     * <p>Solo le prescrizioni in stato {@code DRAFT} o {@code ISSUED} possono essere annullate.</p>
     */
    @Transactional
    public void cancel(Long prescriptionId, Authentication auth) {
        Long doctorId = JwtClaimUtils.requireDoctorId(auth);
        Prescription p = prescriptions.findDetailedById(prescriptionId)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", prescriptionId));

        requireNotCancelled(p);
        deptGuard.checkCanManage(p.getDepartmentCode(), auth);
        consentClient.assertPrescriptionConsent(p.getPatientId(), doctorId, auth);

        p.markCancelled();
        Prescription saved = prescriptions.save(p);

        events.publish(
                AppConstants.Outbox.AGGREGATE_PRESCRIPTION,
                String.valueOf(saved.getId()),
                AppConstants.Outbox.EVT_PRESCRIPTION_CANCELLED,
                new PrescriptionEventPayload(saved),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    // ---------------------------------------------------------------------------
    // STATUS GUARDS
    // ---------------------------------------------------------------------------

    /**
     * Verifica che la prescrizione sia in uno stato modificabile (DRAFT).
     * Le prescrizioni ISSUED o CANCELLED non possono essere aggiornate.
     */
    private void requireMutableStatus(Prescription p) {
        if (p.getStatus() != PrescriptionStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La prescrizione in stato " + p.getStatus() + " non può essere modificata. Solo le prescrizioni in bozza (DRAFT) sono aggiornabili.");
        }
    }

    /**
     * Verifica che la prescrizione non sia già annullata.
     * Sia DRAFT che ISSUED possono essere annullate.
     */
    private void requireNotCancelled(Prescription p) {
        if (p.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La prescrizione è già stata annullata.");
        }
    }

    /**
     * Payload minimale per gli eventi di dominio pubblicati tramite outbox.
     *
     * <p>
     * È volutamente compatto: i consumer possono arricchirlo interrogando i servizi sorgente
     * quando necessario.
     * </p>
     */
    public record PrescriptionEventPayload(
            Long prescriptionId,
            Long patientId,
            Long doctorId,
            String departmentCode,
            PrescriptionStatus status,
            Instant occurredAt
    ) {
        public PrescriptionEventPayload(Prescription p) {
            this(p.getId(), p.getPatientId(), p.getDoctorId(), p.getDepartmentCode(), p.getStatus(), Instant.now());
        }
    }

    /**
     * API amministrativa: lista prescrizioni senza vincoli di consenso/reparto.
     *
     * <p>
     * In produzione questa API dovrebbe essere esposta solo a profili amministrativi e tracciata
     * in modo adeguato (audit).
     * </p>
     */
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> adminList(Long patientId, Long doctorId, Pageable pageable) {
        Page<Prescription> page;
        if (patientId != null) {
            page = prescriptions.findByPatientId(patientId, pageable);
        } else if (doctorId != null) {
            page = prescriptions.findByDoctorId(doctorId, pageable);
        } else {
            page = prescriptions.findAllWithItems(pageable);
        }
        return page.map(mapper::toDto);
    }

    /**
     * API amministrativa: dettaglio prescrizione.
     */
    @Transactional(readOnly = true)
    public PrescriptionDto adminGet(Long id) {
        Prescription p = prescriptions.findDetailedById(id)
                .orElseThrow(() -> NotFoundException.of("Prescrizione", id));
        return mapper.toDto(p);
    }

}
