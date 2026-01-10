package it.sanitech.directory.web;

import it.sanitech.directory.services.SpecializationService;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.commons.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API pubblica (autenticata) per consultazione specializzazioni.
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
