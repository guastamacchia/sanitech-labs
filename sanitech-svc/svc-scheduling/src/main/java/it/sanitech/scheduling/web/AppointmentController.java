package it.sanitech.scheduling.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.scheduling.services.AppointmentService;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import it.sanitech.scheduling.services.dto.create.AppointmentCreateDto;
import it.sanitech.scheduling.services.dto.update.AppointmentReassignDto;
import it.sanitech.scheduling.services.dto.update.AppointmentRescheduleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API per prenotazione e consultazione appuntamenti.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @Auditable(aggregateType = "APPOINTMENT", eventType = "APPOINTMENT_BOOKED", aggregateIdSpel = "id")
    public AppointmentDto book(@Valid @RequestBody AppointmentCreateDto dto, Authentication auth) {
        return appointmentService.book(dto, auth);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT','DOCTOR')")
    public Page<AppointmentDto> search(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(defaultValue = "startAt,desc") String[] sort,
            Authentication auth
    ) {
        return appointmentService.search(patientId, doctorId, department, page, size, sort, auth);
    }

    /**
     * Segna un appuntamento come completato (visita conclusa).
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public AppointmentDto complete(@PathVariable Long id, Authentication auth) {
        return appointmentService.complete(id, auth);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(aggregateType = "APPOINTMENT", eventType = "APPOINTMENT_RESCHEDULED", aggregateIdParam = "id")
    public AppointmentDto reschedule(@PathVariable Long id,
                                     @Valid @RequestBody AppointmentRescheduleDto dto,
                                     Authentication auth) {
        return appointmentService.reschedule(id, dto, auth);
    }

    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(aggregateType = "APPOINTMENT", eventType = "APPOINTMENT_REASSIGNED", aggregateIdParam = "id")
    public AppointmentDto reassign(@PathVariable Long id,
                                   @Valid @RequestBody AppointmentReassignDto dto,
                                   Authentication auth) {
        return appointmentService.reassign(id, dto, auth);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT','DOCTOR')")
    @Auditable(aggregateType = "APPOINTMENT", eventType = "APPOINTMENT_CANCELLED", aggregateIdParam = "id")
    public void cancel(@PathVariable Long id, Authentication auth) {
        appointmentService.cancel(id, auth);
    }

    /**
     * Elimina definitivamente un appuntamento non in stato BOOKED.
     */
    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasRole('ADMIN')")
    public void forceDelete(@PathVariable Long id, Authentication auth) {
        appointmentService.forceDelete(id, auth);
    }
}
