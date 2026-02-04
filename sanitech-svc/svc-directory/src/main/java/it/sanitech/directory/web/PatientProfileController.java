package it.sanitech.directory.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.NotificationPreferenceDto;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.update.PatientPhoneUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API per la gestione del profilo personale del paziente.
 *
 * <p>
 * Espone endpoint per permettere ai pazienti di visualizzare e modificare
 * i propri dati di contatto. L'email non è modificabile in quanto corrisponde
 * allo username utilizzato per l'accesso al portale.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.PATIENT_ME)
@PreAuthorize("hasAuthority('ROLE_PATIENT')")
public class PatientProfileController {

    private final PatientService patientService;

    /**
     * Restituisce i dati del paziente autenticato.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto getProfile(Authentication auth) {
        return patientService.getByEmail(auth.getName());
    }

    /**
     * Aggiorna il numero di telefono del paziente autenticato.
     * L'email non può essere modificata.
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_PHONE_UPDATED", aggregateIdSpel = "id")
    public PatientDto updatePhone(@Valid @RequestBody PatientPhoneUpdateDto dto, Authentication auth) {
        return patientService.updatePhone(auth.getName(), dto, auth);
    }

    // ==================== NOTIFICATION PREFERENCES ====================

    /**
     * Restituisce le preferenze di notifica del paziente autenticato.
     */
    @GetMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public NotificationPreferenceDto getNotificationPreferences(Authentication auth) {
        return patientService.getNotificationPreferences(auth.getName());
    }

    /**
     * Aggiorna le preferenze di notifica del paziente autenticato.
     */
    @PutMapping(value = "/preferences", produces = MediaType.APPLICATION_JSON_VALUE)
    @Auditable(aggregateType = "PATIENT", eventType = "PATIENT_PREFERENCES_UPDATED", aggregateIdSpel = "id")
    public NotificationPreferenceDto updateNotificationPreferences(
            @RequestBody NotificationPreferenceDto dto,
            Authentication auth) {
        return patientService.updateNotificationPreferences(auth.getName(), dto);
    }
}
