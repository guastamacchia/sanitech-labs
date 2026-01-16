package it.sanitech.directory.web;

import it.sanitech.directory.services.SpecializationService;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API pubblica (autenticata) per la consultazione delle specializzazioni.
 *
 * <p>
 * Espone la lista completa delle specializzazioni disponibili per i client autenticati.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.SPECIALIZATIONS)
public class SpecializationController {

    private final SpecializationService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SpecializationDto> list() {
        return service.list();
    }
}
