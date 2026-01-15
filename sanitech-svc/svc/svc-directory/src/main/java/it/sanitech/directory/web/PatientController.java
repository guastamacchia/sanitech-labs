package it.sanitech.directory.web;

import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.utilities.AppConstants;
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
 * Espone endpoint di lettura per utenti ADMIN e DOCTOR. In base alle authority {@code DEPT_*},
 * applica regole ABAC che limitano la visibilità ai soli reparti consentiti.
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
        return SecurityUtils.isAdmin(auth)
                ? patientService.searchAdmin(q, department, page, size, sort)
                : patientService.searchForDoctor(q, page, size, sort, auth);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto get(@PathVariable Long id, Authentication auth) {
        return SecurityUtils.isAdmin(auth)
                ? patientService.get(id)
                : patientService.getForDoctor(id, auth);
    }
}
