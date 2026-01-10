package it.sanitech.directory.web;

import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.commons.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API di consultazione pazienti.
 *
 * <p>
 * Regola principale: un utente DOCTOR vede solo i pazienti dei reparti presenti nelle sue authority {@code DEPT_*}.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.PATIENTS)
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_DOCTOR')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<PatientDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.ROLE_PREFIX + "ADMIN"));

        return isAdmin
                ? patientService.searchAdmin(q, department, page, size, sort)
                : patientService.searchForDoctor(q, page, size, sort, auth);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto get(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.Security.ROLE_PREFIX + "ADMIN"));

        return isAdmin
                ? patientService.get(id)
                : patientService.getForDoctor(id, auth);
    }
}
