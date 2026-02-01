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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        return doctorService.search(q, department, facility, page, size, sort);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }

    @PatchMapping("/{id}/disable")
    public void disable(@PathVariable Long id) {
        doctorService.disableAccess(id);
    }
}
