package it.sanitech.admissions.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.services.AdmissionService;
import it.sanitech.admissions.services.dto.AdmissionDto;
import it.sanitech.admissions.utilities.AppConstants;
import it.sanitech.commons.utilities.SortUtils;
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

/**
 * API del paziente per consultare i propri ricoveri.
 */
@RestController
@RequestMapping("/api/admissions/me")
@RequiredArgsConstructor
public class PatientAdmissionController {

    private final AdmissionService admissionService;

    /**
     * Lista i ricoveri del paziente autenticato.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @RateLimiter(name = "admissionsApi")
    public Page<AdmissionDto> listMine(
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        Sort s = SortUtils.safeSort(sort, AppConstants.SortFields.ADMISSIONS, "admittedAt");
        Pageable pageable = PageRequest.of(page, size, s);
        return admissionService.listMine(status, pageable, auth);
    }
}
