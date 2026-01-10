package it.sanitech.directory.web;

import it.sanitech.directory.services.DepartmentService;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.commons.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API pubblica (autenticata) per consultazione reparti.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.DEPARTMENTS)
public class DepartmentController {

    private final DepartmentService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DepartmentDto> list() {
        return service.list();
    }
}
