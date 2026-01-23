package it.sanitech.directory.web;

import it.sanitech.directory.services.DepartmentService;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrativa per la gestione dei reparti.
 *
 * <p>
 * Consente operazioni di CRUD e ricerca semplice, protette da autorizzazione
 * con ruolo amministrativo.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_DEPARTMENTS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DepartmentAdminController {

    private final DepartmentService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DepartmentDto create(@Valid @RequestBody DepartmentCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DepartmentDto update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateDto dto) {
        return service.update(id, dto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public java.util.List<DepartmentDto> search(@RequestParam(required = false) String q) {
        return service.search(q);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
