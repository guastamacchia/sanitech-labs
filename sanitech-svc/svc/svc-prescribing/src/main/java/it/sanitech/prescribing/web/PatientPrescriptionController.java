package it.sanitech.prescribing.web;

import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * API del paziente per consultare le proprie prescrizioni.
 */
@RestController
@RequestMapping(AppConstants.ApiPath.PRESCRIPTIONS)
@RequiredArgsConstructor
public class PatientPrescriptionController {

    private static final Set<String> ALLOWED_SORT = Set.of(
            "id", "createdAt", "updatedAt", "issuedAt", "status"
    );

    private final PrescriptionService service;

    /**
     * Lista le prescrizioni del paziente autenticato.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN') or hasAuthority('SCOPE_prescriptions.read')")
    @RateLimiter(name = "prescribingApi")
    public Page<PrescriptionDto> listMine(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        Sort s = SortUtils.safeSort(sort, ALLOWED_SORT, "createdAt");
        Pageable pageable = PageRequest.of(page, size, s);
        return service.listMine(status, pageable, auth);
    }

    /**
     * Dettaglio di una prescrizione (solo se appartenente al paziente autenticato).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN') or hasAuthority('SCOPE_prescriptions.read')")
    @RateLimiter(name = "prescribingApi")
    public PrescriptionDto getMine(@PathVariable("id") Long id, Authentication auth) {
        return service.getMine(id, auth);
    }
}
