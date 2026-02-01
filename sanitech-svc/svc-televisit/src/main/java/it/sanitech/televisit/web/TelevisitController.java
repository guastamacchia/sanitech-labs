package it.sanitech.televisit.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.services.TelevisitService;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API operativa per la gestione delle sessioni di video-visita.
 */
@RestController
@RequestMapping(AppConstants.ApiPath.TELEVISITS)
@RequiredArgsConstructor
public class TelevisitController {

    private final TelevisitService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @RateLimiter(name = "televisitApi")
    public Page<TelevisitDto> search(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) TelevisitStatus status,
            @RequestParam(required = false) String doctorSubject,
            @RequestParam(required = false) String patientSubject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.TELEVISIT_SESSION_ALLOWED, AppConstants.SortField.DEFAULT_FIELD);
        Pageable pageable = PageRequest.of(page, size, safeSort);
        return service.search(department, status, doctorSubject, patientSubject, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public TelevisitDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/token/doctor")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @RateLimiter(name = "televisitApi")
    public LiveKitTokenDto doctorToken(@PathVariable Long id, Authentication auth) {
        return service.issueDoctorToken(id, auth);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public TelevisitDto start(@PathVariable Long id, Authentication auth) {
        return service.start(id, auth);
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public TelevisitDto end(@PathVariable Long id, Authentication auth) {
        return service.end(id, auth);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public TelevisitDto cancel(@PathVariable Long id, Authentication auth) {
        return service.cancel(id, auth);
    }
}
