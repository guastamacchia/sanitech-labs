package it.sanitech.scheduling.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.scheduling.clients.DirectoryClient;
import it.sanitech.scheduling.repositories.AppointmentRepository;
import it.sanitech.scheduling.repositories.SlotRepository;
import it.sanitech.scheduling.repositories.entities.*;
import it.sanitech.scheduling.security.JwtClaimUtils;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import it.sanitech.scheduling.services.dto.create.AppointmentCreateDto;
import it.sanitech.scheduling.services.dto.update.AppointmentReassignDto;
import it.sanitech.scheduling.services.dto.update.AppointmentRescheduleDto;
import it.sanitech.scheduling.services.mapper.AppointmentMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service di dominio per la gestione degli {@link Appointment}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final SlotRepository slots;
    private final AppointmentMapper appointmentMapper;
    private final DomainEventPublisher events;
    private final DirectoryClient directoryClient;

    /**
     * Prenota un appuntamento su uno slot disponibile.
     *
     * <p>
     * La prenotazione usa lock pessimistica sullo slot per prevenire doppie prenotazioni concorrenti.
     * </p>
     */
    @Transactional
    public AppointmentDto book(AppointmentCreateDto dto, Authentication auth) {
        Long patientId = resolvePatientId(dto.patientId(), auth);

        Slot slot = slots.findByIdForUpdate(dto.slotId())
                .orElseThrow(() -> NotFoundException.of("Slot", dto.slotId()));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_NOT_AVAILABLE);
        }

        slot.markBooked();
        slots.save(slot);

        Appointment appt = Appointment.builder()
                .slotId(slot.getId())
                .patientId(patientId)
                .doctorId(slot.getDoctorId())
                .departmentCode(slot.getDepartmentCode())
                .mode(slot.getMode())
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .status(AppointmentStatus.BOOKED)
                .reason(dto.reason())
                .build();

        Appointment saved = appointments.save(appt);

        events.publish(
                "APPOINTMENT",
                String.valueOf(saved.getId()),
                "APPOINTMENT_BOOKED",
                Map.of(
                        "appointmentId", saved.getId(),
                        "slotId", saved.getSlotId(),
                        "patientId", saved.getPatientId(),
                        "doctorId", saved.getDoctorId(),
                        "departmentCode", saved.getDepartmentCode(),
                        "mode", saved.getMode().name(),
                        "startAt", saved.getStartAt().toString(),
                        "endAt", saved.getEndAt().toString()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return appointmentMapper.toDto(saved);
    }

    /**
     * Elenco appuntamenti con logica "self" per PATIENT/DOCTOR.
     * <p>
     * - ADMIN: può filtrare liberamente (patientId/doctorId/dept)
     * - PATIENT: vede solo i propri appuntamenti (claim {@code pid})
     * - DOCTOR: vede solo i propri appuntamenti (claim {@code did})
     * </p>
     */
    @Bulkhead(name = "schedulingRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<AppointmentDto> search(
            Long patientId,
            Long doctorId,
            String departmentCode,
            int page,
            int size,
            String[] sort,
            Authentication auth
    ) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.Sorting.ALLOWED_APPOINTMENT_SORT_FIELDS, "startAt");
        Pageable pageable = PageableUtils.pageRequest(page, size, AppConstants.MAX_PAGE_SIZE, safeSort);

        Specification<Appointment> spec = (root, query, cb) -> cb.conjunction();

        if (SecurityUtils.isAdmin(auth)) {
            spec = spec
                    .and(optionalEq("patientId", patientId))
                    .and(optionalEq("doctorId", doctorId))
                    .and(optionalEqIgnoreCase("departmentCode", departmentCode));
        } else if (SecurityUtils.isPatient(auth)) {
            Long pid = JwtClaimUtils.requireLongClaim(auth, AppConstants.JwtClaims.PATIENT_ID);
            spec = spec.and(optionalEq("patientId", pid));
        } else if (SecurityUtils.isDoctor(auth)) {
            Long did = JwtClaimUtils.requireLongClaim(auth, AppConstants.JwtClaims.DOCTOR_ID);
            spec = spec.and(optionalEq("doctorId", did));
        } else {
            // ruolo non gestito
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_ROLE_NOT_ALLOWED_APPOINTMENT_SEARCH);
        }

        return appointments.findAll(spec, pageable).map(appointmentMapper::toDto);
    }

    /**
     * Cancella un appuntamento e libera lo slot.
     */
    @Transactional
    public void cancel(Long appointmentId, Authentication auth) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Appointment", appointmentId));

        // Autorizzazione: admin oppure patient-owner.
        if (!SecurityUtils.isAdmin(auth)) {
            Long pid = JwtClaimUtils.requireLongClaim(auth, AppConstants.JwtClaims.PATIENT_ID);
            if (!pid.equals(appt.getPatientId())) {
                throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_APPOINTMENT_CANCEL_NOT_AUTHORIZED);
            }
        }

        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            return; // idempotenza
        }

        appt.cancel(Instant.now());
        appointments.save(appt);

        // Libera slot con lock per evitare competizione con nuove prenotazioni.
        Slot slot = slots.findByIdForUpdate(appt.getSlotId())
                .orElseThrow(() -> NotFoundException.of("Slot", appt.getSlotId()));
        if (slot.getStatus() == SlotStatus.BOOKED) {
            slot.markAvailable();
            slots.save(slot);
        }

        events.publish(
                "APPOINTMENT",
                String.valueOf(appt.getId()),
                "APPOINTMENT_CANCELLED",
                Map.of("appointmentId", appt.getId(), "occurredAt", Instant.now().toString()),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    /**
     * Segna un appuntamento come completato e pubblica eventi su 3 topic.
     */
    @Transactional
    public AppointmentDto complete(Long appointmentId, Authentication auth) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Appointment", appointmentId));

        if (appt.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException("L'appuntamento può essere completato solo se in stato BOOKED.");
        }

        appt.complete(Instant.now());
        Appointment saved = appointments.save(appt);

        // Arricchimento dati anagrafici da svc-directory
        DirectoryClient.PersonInfo patientInfo = directoryClient.findPatientById(saved.getPatientId());
        DirectoryClient.PersonInfo doctorInfo = directoryClient.findDoctorById(saved.getDoctorId());

        String patientName = patientInfo != null ? patientInfo.fullName() : null;
        String patientEmail = patientInfo != null ? patientInfo.email() : null;
        String doctorName = doctorInfo != null ? doctorInfo.fullName() : null;
        String doctorEmail = doctorInfo != null ? doctorInfo.email() : null;

        // 1. Evento audit
        events.publish("APPOINTMENT", String.valueOf(saved.getId()), "APPOINTMENT_COMPLETED",
                Map.of(
                        "appointmentId", saved.getId(),
                        "patientId", saved.getPatientId(),
                        "doctorId", saved.getDoctorId(),
                        "departmentCode", saved.getDepartmentCode(),
                        "mode", saved.getMode().name(),
                        "completedAt", saved.getCompletedAt().toString()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        // 2. Evento payments (payload arricchito per fatturazione)
        Map<String, Object> paymentsPayload = new HashMap<>();
        paymentsPayload.put("sourceId", saved.getId());
        paymentsPayload.put("sourceType", "APPOINTMENT");
        paymentsPayload.put("patientId", saved.getPatientId());
        paymentsPayload.put("patientName", patientName);
        paymentsPayload.put("patientEmail", patientEmail);
        paymentsPayload.put("doctorId", saved.getDoctorId());
        paymentsPayload.put("doctorName", doctorName);
        paymentsPayload.put("departmentCode", saved.getDepartmentCode());
        paymentsPayload.put("mode", saved.getMode().name());
        paymentsPayload.put("startAt", saved.getStartAt().toString());
        paymentsPayload.put("endAt", saved.getEndAt().toString());
        paymentsPayload.put("completedAt", saved.getCompletedAt().toString());

        events.publish("APPOINTMENT", String.valueOf(saved.getId()), "APPOINTMENT_COMPLETED",
                paymentsPayload, AppConstants.Outbox.TOPIC_PAYMENTS_EVENTS, auth);

        // 3. Evento notifications (email a medico e paziente)
        Map<String, Object> notificationsPayload = new HashMap<>();
        notificationsPayload.put("notificationType", "APPOINTMENT_COMPLETED");
        notificationsPayload.put("sourceId", saved.getId());
        notificationsPayload.put("departmentCode", saved.getDepartmentCode());
        notificationsPayload.put("mode", saved.getMode().name());
        notificationsPayload.put("patientName", patientName);
        notificationsPayload.put("patientEmail", patientEmail);
        notificationsPayload.put("doctorName", doctorName);
        notificationsPayload.put("doctorEmail", doctorEmail);
        notificationsPayload.put("completedAt", saved.getCompletedAt().toString());

        events.publish("APPOINTMENT", String.valueOf(saved.getId()), "APPOINTMENT_COMPLETED",
                notificationsPayload, AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS, auth);

        return appointmentMapper.toDto(saved);
    }

    /**
     * Ripianifica un appuntamento spostando su un nuovo slot dello stesso medico.
     */
    @Transactional
    public AppointmentDto reschedule(Long appointmentId, AppointmentRescheduleDto dto, Authentication auth) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Appointment", appointmentId));

        if (appt.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_APPOINTMENT_NOT_BOOKED);
        }
        if (appt.getSlotId().equals(dto.newSlotId())) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SAME_SLOT);
        }

        Slot oldSlot = slots.findByIdForUpdate(appt.getSlotId())
                .orElseThrow(() -> NotFoundException.of("Slot", appt.getSlotId()));
        oldSlot.markAvailable();
        slots.save(oldSlot);

        Slot newSlot = slots.findByIdForUpdate(dto.newSlotId())
                .orElseThrow(() -> NotFoundException.of("Slot", dto.newSlotId()));
        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_NOT_AVAILABLE);
        }
        if (!newSlot.getDoctorId().equals(appt.getDoctorId())) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_DOCTOR_MISMATCH);
        }
        if (newSlot.getMode() != appt.getMode()) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_MODE_MISMATCH);
        }
        newSlot.markBooked();
        slots.save(newSlot);

        // Rilascia il vincolo unique slot_id su appuntamenti non-BOOKED che occupano ancora lo slot
        appointments.findBySlotId(newSlot.getId()).ifPresent(stale -> {
            if (stale.getStatus() != AppointmentStatus.BOOKED) {
                stale.setSlotId(null);
                appointments.saveAndFlush(stale);
            }
        });

        appt.setSlotId(newSlot.getId());
        appt.setStartAt(newSlot.getStartAt());
        appt.setEndAt(newSlot.getEndAt());
        Appointment saved = appointments.save(appt);

        events.publish("APPOINTMENT", String.valueOf(saved.getId()), "APPOINTMENT_RESCHEDULED",
                Map.of(
                        "appointmentId", saved.getId(),
                        "oldSlotId", oldSlot.getId(),
                        "newSlotId", newSlot.getId(),
                        "doctorId", saved.getDoctorId(),
                        "oldStartAt", oldSlot.getStartAt().toString(),
                        "newStartAt", newSlot.getStartAt().toString()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        return appointmentMapper.toDto(saved);
    }

    /**
     * Riassegna un appuntamento a un nuovo medico con un nuovo slot.
     */
    @Transactional
    public AppointmentDto reassign(Long appointmentId, AppointmentReassignDto dto, Authentication auth) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Appointment", appointmentId));

        if (appt.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_APPOINTMENT_NOT_BOOKED);
        }

        Slot oldSlot = slots.findByIdForUpdate(appt.getSlotId())
                .orElseThrow(() -> NotFoundException.of("Slot", appt.getSlotId()));
        oldSlot.markAvailable();
        slots.save(oldSlot);

        Slot newSlot = slots.findByIdForUpdate(dto.newSlotId())
                .orElseThrow(() -> NotFoundException.of("Slot", dto.newSlotId()));
        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_NOT_AVAILABLE);
        }
        if (!newSlot.getDoctorId().equals(dto.newDoctorId())) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_NEW_DOCTOR_SLOT_MISMATCH);
        }
        if (newSlot.getMode() != appt.getMode()) {
            throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_SLOT_MODE_MISMATCH);
        }
        newSlot.markBooked();
        slots.save(newSlot);

        // Rilascia il vincolo unique slot_id su appuntamenti non-BOOKED che occupano ancora lo slot
        appointments.findBySlotId(newSlot.getId()).ifPresent(stale -> {
            if (stale.getStatus() != AppointmentStatus.BOOKED) {
                stale.setSlotId(null);
                appointments.saveAndFlush(stale);
            }
        });

        appt.setSlotId(newSlot.getId());
        appt.setDoctorId(newSlot.getDoctorId());
        appt.setDepartmentCode(newSlot.getDepartmentCode());
        appt.setStartAt(newSlot.getStartAt());
        appt.setEndAt(newSlot.getEndAt());
        Appointment saved = appointments.save(appt);

        events.publish("APPOINTMENT", String.valueOf(saved.getId()), "APPOINTMENT_REASSIGNED",
                Map.of(
                        "appointmentId", saved.getId(),
                        "oldDoctorId", oldSlot.getDoctorId(),
                        "newDoctorId", saved.getDoctorId(),
                        "oldSlotId", oldSlot.getId(),
                        "newSlotId", newSlot.getId(),
                        "oldDepartmentCode", oldSlot.getDepartmentCode(),
                        "newDepartmentCode", saved.getDepartmentCode()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        return appointmentMapper.toDto(saved);
    }

    /**
     * Elimina definitivamente un appuntamento. Solo per ADMIN e solo se non BOOKED.
     */
    @Transactional
    public void forceDelete(Long appointmentId, Authentication auth) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> NotFoundException.of("Appointment", appointmentId));

        if (appt.getStatus() == AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException("Non è possibile eliminare un appuntamento in stato BOOKED. Annullarlo prima.");
        }

        appointments.delete(appt);

        events.publish(
                "APPOINTMENT",
                String.valueOf(appt.getId()),
                "APPOINTMENT_DELETED",
                Map.of("appointmentId", appt.getId(), "occurredAt", Instant.now().toString()),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    private static Long resolvePatientId(Long patientIdFromBody, Authentication auth) {
        if (SecurityUtils.isAdmin(auth)) {
            if (patientIdFromBody == null) {
                throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_PATIENT_ID_REQUIRED_FOR_ADMIN);
            }
            return patientIdFromBody;
        }

        if (SecurityUtils.isPatient(auth)) {
            Long pid = JwtClaimUtils.requireLongClaim(auth, AppConstants.JwtClaims.PATIENT_ID);
            if (patientIdFromBody != null && !pid.equals(patientIdFromBody)) {
                throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_PATIENT_ID_MISMATCH);
            }
            return pid;
        }

        throw new IllegalArgumentException(AppConstants.ErrorMessage.MSG_BOOKING_ROLE_NOT_ALLOWED);
    }

    private static Specification<Appointment> optionalEq(String field, Object value) {
        return (root, query, cb) -> value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }

    private static Specification<Appointment> optionalEqIgnoreCase(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get(field)), value.toUpperCase());
        };
    }
}
