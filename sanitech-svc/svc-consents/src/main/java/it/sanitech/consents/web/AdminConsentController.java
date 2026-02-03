package it.sanitech.consents.web;

import it.sanitech.consents.services.ConsentService;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrative per la gestione dei consensi.
 * <p>
 * Tipicamente usate da backoffice o operatori autorizzati.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_CONSENTS)
@PreAuthorize("hasRole('ADMIN')")
public class AdminConsentController {

    private final ConsentService service;

    @GetMapping("/{id}")
    public ConsentDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        service.deleteById(id, auth);
    }
}
