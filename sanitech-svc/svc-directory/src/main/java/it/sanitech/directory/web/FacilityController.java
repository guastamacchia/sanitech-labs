package it.sanitech.directory.web;

import it.sanitech.directory.services.FacilityService;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API pubblica (autenticata) per la consultazione delle strutture.
 *
 * <p>
 * Espone la lista completa delle strutture sanitarie del network per i client autenticati.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.FACILITIES)
public class FacilityController {

    private final FacilityService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FacilityDto> list() {
        return service.list();
    }
}
