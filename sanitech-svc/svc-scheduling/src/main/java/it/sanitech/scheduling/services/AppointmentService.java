package it.sanitech.scheduling.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.commons.utilities.PageableUtils;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.scheduling.repositories.AppointmentRepository;
import it.sanitech.scheduling.repositories.SlotRepository;
import it.sanitech.scheduling.repositories.entities.*;
import it.sanitech.scheduling.security.JwtClaimUtils;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import it.sanitech.scheduling.services.dto.create.AppointmentCreateDto;
import it.sanitech.scheduling.services.mapper.AppointmentMapper;
import it.sanitech.scheduling.utilities.AppConstants;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

/**
 * Service di dominio per la gestione degli {@link Appointment}.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final SlotRepository slots;
    private final AppointmentMapper appointmentMapper;
    private final DomainEventPublisher events;

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
