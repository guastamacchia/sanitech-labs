package it.sanitech.directory.web;

import it.sanitech.directory.services.SpecializationService;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.services.dto.create.SpecializationCreateDto;
import it.sanitech.directory.services.dto.update.SpecializationUpdateDto;
import it.sanitech.commons.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrativa per gestione specializzazioni.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_SPECIALIZATIONS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SpecializationAdminController {

    private final SpecializationService service;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public SpecializationDto create(@Valid @RequestBody SpecializationCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpecializationDto update(@PathVariable Long id, @Valid @RequestBody SpecializationUpdateDto dto) {
        return service.update(id, dto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public java.util.List<SpecializationDto> search(@RequestParam(required = false) String q) {
        return service.search(q);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
