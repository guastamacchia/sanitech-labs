package it.sanitech.directory.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.update.DoctorPhoneUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API per la gestione del profilo personale del medico.
 *
 * <p>
 * Espone endpoint per permettere ai medici di visualizzare e modificare
 * i propri dati di contatto. L'email non è modificabile in quanto corrisponde
 * allo username utilizzato per l'accesso al portale.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.DOCTOR_ME)
@PreAuthorize("hasAuthority('ROLE_DOCTOR')")
public class DoctorProfileController {

    private final DoctorService doctorService;

    /**
     * Restituisce i dati del medico autenticato.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto getProfile(Authentication auth) {
        return doctorService.getByEmail(auth.getName());
    }

    /**
     * Aggiorna il numero di telefono del medico autenticato.
     * L'email non può essere modificata.
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "DOCTOR", eventType = "DOCTOR_PHONE_UPDATED", aggregateIdSpel = "id")
    public DoctorDto updatePhone(@Valid @RequestBody DoctorPhoneUpdateDto dto, Authentication auth) {
        return doctorService.updatePhone(auth.getName(), dto, auth);
    }
}
