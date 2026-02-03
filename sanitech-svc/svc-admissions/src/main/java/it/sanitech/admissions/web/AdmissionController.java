package it.sanitech.admissions.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.services.AdmissionService;
import it.sanitech.admissions.services.dto.AdmissionDto;
import it.sanitech.admissions.services.dto.create.AdmissionCreateDto;
import it.sanitech.admissions.utilities.AppConstants;
import it.sanitech.commons.audit.Auditable;
import it.sanitech.commons.utilities.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * API REST per la gestione dei ricoveri.
 */
@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    /**
     * Lista/ricerca ricoveri (paginata), con rate-limit.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @RateLimiter(name = "admissionsApi")
    public Page<AdmissionDto> list(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortFields.ADMISSIONS, "admittedAt");
        Pageable pageable = PageRequest.of(safePage, safeSize, safeSort);

        return admissionService.list(auth, department, status, pageable);
    }

    /**
     * Crea un nuovo ricovero.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Auditable(aggregateType = "ADMISSION", eventType = "ADMISSION_CREATED", aggregateIdSpel = "id")
    public ResponseEntity<AdmissionDto> admit(@Valid @RequestBody AdmissionCreateDto body, Authentication auth) {
        AdmissionDto created = admissionService.admit(body, auth);
        return ResponseEntity
                .created(URI.create("/api/admissions/" + created.id()))
                .body(created);
    }

    /**
     * Dimette un ricovero attivo.
     */
    @PostMapping("/{id}/discharge")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @Auditable(aggregateType = "ADMISSION", eventType = "ADMISSION_DISCHARGED", aggregateIdParam = "id")
    public AdmissionDto discharge(@PathVariable("id") Long id, Authentication auth) {
        return admissionService.discharge(id, auth);
    }
}
