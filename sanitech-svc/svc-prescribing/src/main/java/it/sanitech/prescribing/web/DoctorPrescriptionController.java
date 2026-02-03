package it.sanitech.prescribing.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.prescribing.services.PrescriptionService;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionCreateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionPatchDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionUpdateDto;
import it.sanitech.prescribing.utilities.AppConstants;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * API per i medici: crea e consulta prescrizioni dei pazienti.
 */
@RestController
@RequestMapping(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS)
@RequiredArgsConstructor
public class DoctorPrescriptionController {

    private static final Set<String> ALLOWED_SORT = Set.of(
            "id", "createdAt", "updatedAt", "issuedAt", "status"
    );

    private final PrescriptionService service;

    /**
     * Crea una nuova prescrizione per un paziente (consenso richiesto).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.write')")
    @RateLimiter(name = "prescribingApi")
    @Auditable(aggregateType = "PRESCRIPTION", eventType = "PRESCRIPTION_CREATED", aggregateIdSpel = "id")
    public PrescriptionDto create(@Valid @RequestBody PrescriptionCreateDto dto, Authentication auth) {
        return service.create(dto, auth);
    }

    /**
     * Lista le prescrizioni di un paziente all'interno di un reparto.
     *
     * <p>
     * Il filtro {@code departmentCode} evita esposizioni cross-department.
     * </p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.read')")
    @RateLimiter(name = "prescribingApi")
    public Page<PrescriptionDto> listForPatient(
            @RequestParam Long patientId,
            @RequestParam String departmentCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        Sort s = SortUtils.safeSort(sort, ALLOWED_SORT, "createdAt");
        Pageable pageable = PageRequest.of(page, size, s);
        return service.listForDoctor(patientId, departmentCode, pageable, auth);
    }

    /**
     * Dettaglio prescrizione (consenso richiesto).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.read')")
    @RateLimiter(name = "prescribingApi")
    public PrescriptionDto get(@PathVariable("id") Long id, Authentication auth) {
        return service.getForDoctor(id, auth);
    }

    /**
     * Aggiornamento completo (replace righe).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.write')")
    @RateLimiter(name = "prescribingApi")
    @Auditable(aggregateType = "PRESCRIPTION", eventType = "PRESCRIPTION_UPDATED", aggregateIdParam = "id")
    public PrescriptionDto update(
            @PathVariable("id") Long id,
            @Valid @RequestBody PrescriptionUpdateDto dto,
            Authentication auth
    ) {
        return service.update(id, dto, auth);
    }

    /**
     * Patch parziale (attualmente solo note).
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.write')")
    @RateLimiter(name = "prescribingApi")
    @Auditable(aggregateType = "PRESCRIPTION", eventType = "PRESCRIPTION_PATCHED", aggregateIdParam = "id")
    public PrescriptionDto patch(
            @PathVariable("id") Long id,
            @RequestBody PrescriptionPatchDto dto,
            Authentication auth
    ) {
        return service.patch(id, dto, auth);
    }

    /**
     * Annulla una prescrizione.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN') or hasAuthority('SCOPE_prescriptions.write')")
    @RateLimiter(name = "prescribingApi")
    @Auditable(aggregateType = "PRESCRIPTION", eventType = "PRESCRIPTION_CANCELLED", aggregateIdParam = "id")
    public void cancel(@PathVariable("id") Long id, Authentication auth) {
        service.cancel(id, auth);
    }
}
