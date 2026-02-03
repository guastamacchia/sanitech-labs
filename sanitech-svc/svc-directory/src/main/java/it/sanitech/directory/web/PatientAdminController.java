package it.sanitech.directory.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrativa per la gestione dei pazienti.
 *
 * <p>
 * Espone operazioni di creazione, aggiornamento parziale, ricerca paginata e cancellazione,
 * riservate agli utenti con ruolo amministrativo.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_PATIENTS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PatientAdminController {

    private final PatientService patientService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_CREATED", aggregateIdSpel = "id")
    public PatientDto create(@Valid @RequestBody PatientCreateDto dto, Authentication auth) {
        return patientService.create(dto, auth);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_UPDATED", aggregateIdParam = "id")
    public PatientDto patch(@PathVariable Long id, @Valid @RequestBody PatientUpdateDto dto, Authentication auth) {
        return patientService.patch(id, dto, auth);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public org.springframework.data.domain.Page<PatientDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        return patientService.searchAdmin(q, department, status, doctorId, page, size, sort);
    }

    @DeleteMapping("/{id}")
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_DELETED", aggregateIdParam = "id")
    public void delete(@PathVariable Long id, Authentication auth) {
        patientService.delete(id, auth);
    }

    @PatchMapping("/{id}/activate")
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_ACTIVATED", aggregateIdParam = "id")
    public PatientDto activate(@PathVariable Long id, Authentication auth) {
        return patientService.activate(id, auth);
    }

    @PatchMapping("/{id}/disable")
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_DISABLED", aggregateIdParam = "id")
    public PatientDto disable(@PathVariable Long id, Authentication auth) {
        return patientService.disableAccess(id, auth);
    }

    @PostMapping("/{id}/resend-activation")
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_ACTIVATION_RESENT", aggregateIdParam = "id")
    public void resendActivation(@PathVariable Long id, Authentication auth) {
        patientService.resendActivation(id, auth);
    }
}
