package it.sanitech.directory.web;

import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrativa per la gestione dei medici.
 *
 * <p>
 * Espone operazioni di creazione, aggiornamento parziale, ricerca paginata e cancellazione,
 * riservate agli utenti con ruolo amministrativo.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_DOCTORS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DoctorAdminController {

    private final DoctorService doctorService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto create(@Valid @RequestBody DoctorCreateDto dto, Authentication auth) {
        return doctorService.create(dto, auth);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto patch(@PathVariable Long id, @Valid @RequestBody DoctorUpdateDto dto, Authentication auth) {
        return doctorService.patch(id, dto, auth);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public org.springframework.data.domain.Page<DoctorDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String facility,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        return doctorService.search(q, department, facility, status, page, size, sort);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        doctorService.delete(id, auth);
    }

    @PatchMapping("/{id}/activate")
    public DoctorDto activate(@PathVariable Long id, Authentication auth) {
        return doctorService.activate(id, auth);
    }

    @PatchMapping("/{id}/disable")
    public DoctorDto disable(@PathVariable Long id, Authentication auth) {
        return doctorService.disableAccess(id, auth);
    }

    @PostMapping("/{id}/resend-activation")
    public void resendActivation(@PathVariable Long id, Authentication auth) {
        doctorService.resendActivation(id, auth);
    }

    @PatchMapping("/{id}/transfer")
    public DoctorDto transfer(@PathVariable Long id, @RequestParam String departmentCode) {
        return doctorService.transfer(id, departmentCode);
    }
}
