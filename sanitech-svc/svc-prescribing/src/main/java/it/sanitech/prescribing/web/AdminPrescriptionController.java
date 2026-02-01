package it.sanitech.prescribing.web;

import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.prescribing.services.PrescriptionService;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.utilities.AppConstants;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * API amministrativa per consultare le prescrizioni (uso backoffice).
 */
@RestController
@RequestMapping(AppConstants.ApiPath.ADMIN_PRESCRIPTIONS)
@RequiredArgsConstructor
public class AdminPrescriptionController {

    private static final Set<String> ALLOWED_SORT = Set.of(
            "id", "createdAt", "updatedAt", "issuedAt", "status", "patientId", "doctorId"
    );

    private final PrescriptionService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_prescriptions.admin')")
    @RateLimiter(name = "prescribingApi")
    public Page<PrescriptionDto> list(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size,
            @RequestParam(required = false) String[] sort
    ) {
        Sort s = SortUtils.safeSort(sort, ALLOWED_SORT, "createdAt");
        Pageable pageable = PageRequest.of(page, size, s);
        return service.adminList(patientId, doctorId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_prescriptions.admin')")
    @RateLimiter(name = "prescribingApi")
    public PrescriptionDto get(@PathVariable("id") Long id) {
        return service.adminGet(id);
    }
}
