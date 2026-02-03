package it.sanitech.directory.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.directory.services.FacilityService;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.services.dto.create.FacilityCreateDto;
import it.sanitech.directory.services.dto.update.FacilityUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API amministrativa per la gestione delle strutture (Facility).
 *
 * <p>
 * Consente creazione, aggiornamento, ricerca e cancellazione delle strutture sanitarie
 * con accesso riservato agli utenti con ruolo amministrativo.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_FACILITIES)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class FacilityAdminController {

    private final FacilityService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "FACILITY", eventType = "FACILITY_CREATED", aggregateIdSpel = "id")
    public FacilityDto create(@Valid @RequestBody FacilityCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "FACILITY", eventType = "FACILITY_UPDATED", aggregateIdParam = "id")
    public FacilityDto update(@PathVariable Long id, @Valid @RequestBody FacilityUpdateDto dto) {
        return service.update(id, dto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FacilityDto> search(@RequestParam(required = false) String q) {
        return service.search(q);
    }

    @DeleteMapping("/{id}")
    @Auditable(aggregateType = "FACILITY", eventType = "FACILITY_DELETED", aggregateIdParam = "id")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
