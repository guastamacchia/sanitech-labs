package it.sanitech.consents.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.security.AuthClaims;
import it.sanitech.consents.services.ConsentService;
import it.sanitech.consents.services.dto.ConsentBulkCreateDto;
import it.sanitech.consents.services.dto.ConsentCheckResponse;
import it.sanitech.consents.services.dto.ConsentCreateDto;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.services.dto.ConsentUpdateDto;
import it.sanitech.consents.services.dto.PrivacyConsentCreateDto;
import it.sanitech.consents.services.dto.PrivacyConsentDto;
import it.sanitech.consents.utilities.AppConstants;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API pubbliche del servizio consensi.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.CONSENTS)
public class ConsentController {

    private final ConsentService service;

    /**
     * Verifica se esiste un consenso valido (GRANTED e non scaduto) per l'accesso del medico ai dati del paziente.
     * <p>
     * Tipicamente usato da servizi clinici o dal medico stesso prima di accedere ai dati del paziente.
     * </p>
     */
    @GetMapping("/check")
    @RateLimiter(name = "consentsApi")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR') or hasAuthority('SCOPE_consents.read')")
    public ConsentCheckResponse check(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam ConsentScope scope,
            Authentication auth
    ) {
        Long resolvedDoctorId = resolveDoctorId(doctorId, auth);
        return service.check(patientId, resolvedDoctorId, scope);
    }

    /**
     * Lista i consensi privacy del paziente autenticato (GDPR, privacy, terapia).
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<PrivacyConsentDto> myConsents(Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.listPrivacyConsentsForPatient(patientId);
    }

    /**
     * Registra un consenso privacy (GDPR, privacy, terapia) per l'utente paziente autenticato.
     */
    @PostMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "PRIVACY_CONSENT_CREATED", aggregateIdSpel = "id")
    public PrivacyConsentDto registerPrivacyConsent(@RequestBody @Valid PrivacyConsentCreateDto dto, Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.registerPrivacyConsent(patientId, dto, auth);
    }

    /**
     * Elimina un consenso privacy per ID.
     */
    @DeleteMapping("/me/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "PRIVACY_CONSENT_DELETED", aggregateIdParam = "id")
    public void deletePrivacyConsent(@PathVariable Long id, Authentication auth) {
        requirePatientId(auth);
        service.deletePrivacyConsentById(id, auth);
    }

    // ======================== Doctor Consent Endpoints ========================

    /**
     * Lista i consensi medico-specifici del paziente autenticato.
     */
    @GetMapping("/me/doctors")
    @PreAuthorize("hasRole('PATIENT')")
    public List<ConsentDto> myDoctorConsents(Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.listForPatient(patientId);
    }

    /**
     * Concede un consenso (paziente → medico) per l'utente paziente autenticato.
     */
    @PostMapping("/me/doctors")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "DOCTOR_CONSENT_GRANTED", aggregateIdSpel = "id")
    public ConsentDto grantDoctorConsent(@RequestBody @Valid ConsentCreateDto dto, Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.grantForPatient(patientId, dto, auth);
    }

    /**
     * Concede consensi multipli (bulk) per un singolo medico.
     * Ogni scope genera un record separato in tabella.
     * <p>
     * Questo endpoint permette al paziente di associare ad un medico tutti i consensi
     * (DOCS, PRESCRIPTIONS, TELEVISIT) in una sola operazione.
     * </p>
     *
     * @param dto DTO con doctorId, set di scope e scadenza opzionale
     * @param auth autenticazione corrente
     * @return lista dei consensi concessi (un record per ogni scope)
     */
    @PostMapping("/me/doctors/bulk")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "DOCTOR_CONSENTS_BULK_GRANTED", aggregateIdSpel = "#dto.doctorId")
    public List<ConsentDto> grantDoctorConsentsBulk(@RequestBody @Valid ConsentBulkCreateDto dto, Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.grantBulkForPatient(patientId, dto, auth);
    }

    /**
     * Revoca un consenso (paziente → medico) per l'utente paziente autenticato.
     */
    @DeleteMapping("/me/doctors/{doctorId}/{scope}")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "DOCTOR_CONSENT_REVOKED", aggregateIdParam = "doctorId")
    public void revokeDoctorConsent(@PathVariable Long doctorId, @PathVariable ConsentScope scope, Authentication auth) {
        Long patientId = requirePatientId(auth);
        service.revokeForPatient(patientId, doctorId, scope, auth);
    }

    /**
     * Aggiorna un consenso esistente (paziente → medico) per l'utente paziente autenticato.
     * Permette di modificare la scadenza del consenso.
     */
    @PatchMapping("/me/doctors/{doctorId}/{scope}")
    @PreAuthorize("hasRole('PATIENT')")
    @Auditable(aggregateType = "CONSENT", eventType = "DOCTOR_CONSENT_UPDATED", aggregateIdParam = "doctorId")
    public ConsentDto updateDoctorConsent(
            @PathVariable Long doctorId,
            @PathVariable ConsentScope scope,
            @RequestBody @Valid ConsentUpdateDto dto,
            Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.updateForPatient(patientId, doctorId, scope, dto, auth);
    }

    /**
     * Restituisce gli ID dei pazienti che hanno concesso consenso TELEVISIT attivo al medico specificato.
     * <p>
     * Utilizzato dal pannello admin per filtrare i pazienti selezionabili nella pianificazione televisite.
     * </p>
     *
     * @param doctorId ID del medico per cui cercare i pazienti con consenso
     * @return lista di patient ID con consenso TELEVISIT valido
     */
    @GetMapping("/patients-with-televisit-consent")
    @RateLimiter(name = "consentsApi")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public List<Long> getPatientsWithTelevisitConsent(@RequestParam Long doctorId, Authentication auth) {
        return service.getPatientIdsWithTelevisitConsent(doctorId);
    }

    /**
     * Restituisce gli ID dei pazienti che hanno concesso consenso attivo al medico per lo scope specificato.
     * <p>
     * Utilizzato dalla dashboard medico per filtrare i pazienti in base al tipo di consenso
     * (es. PRESCRIPTIONS per le prescrizioni, DOCS per i documenti clinici).
     * </p>
     * <p>
     * Se doctorId non è fornito, viene usato l'ID del medico autenticato dal claim "did".
     * </p>
     *
     * @param scope tipo di consenso richiesto
     * @param doctorId ID del medico (opzionale, default dal token)
     * @return lista di patient ID con consenso valido per lo scope
     */
    @GetMapping("/patients-with-consent")
    @RateLimiter(name = "consentsApi")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public List<Long> getPatientsWithConsent(
            @RequestParam ConsentScope scope,
            @RequestParam(required = false) Long doctorId,
            Authentication auth) {
        Long resolvedDoctorId = resolveDoctorId(doctorId, auth);
        return service.getPatientIdsWithConsent(resolvedDoctorId, scope);
    }

    /**
     * Restituisce gli ID dei medici che hanno ricevuto consenso attivo dal paziente specificato.
     * <p>
     * Utilizzato dal pannello admin per la selezione del nuovo medico referente durante
     * il cambio referente di un ricovero: mostra solo i medici autorizzati dal paziente.
     * </p>
     *
     * @param patientId ID del paziente
     * @param scope tipo di consenso richiesto (opzionale, se null ritorna tutti i medici con almeno un consenso attivo)
     * @return lista di doctor ID con consenso valido
     */
    @GetMapping("/doctors-with-consent")
    @RateLimiter(name = "consentsApi")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Long> getDoctorsWithConsent(
            @RequestParam Long patientId,
            @RequestParam(required = false) ConsentScope scope,
            Authentication auth) {
        return service.getDoctorIdsWithConsent(patientId, scope);
    }

    private static Long requirePatientId(Authentication auth) {
        return AuthClaims.patientId(auth)
                .orElseThrow(() -> new AccessDeniedException("Token privo della claim patient id (pid)."));
    }

    private static Long resolveDoctorId(Long doctorIdFromRequest, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (doctorIdFromRequest != null) {
            // Se non admin, un medico può verificare solo per sé (evitiamo enumeration).
            if (!isAdmin) {
                Long tokenDoctorId = AuthClaims.doctorId(auth).orElse(null);
                if (tokenDoctorId != null && !tokenDoctorId.equals(doctorIdFromRequest)) {
                    throw new AccessDeniedException("Non è consentito verificare consensi per un altro medico.");
                }
            }
            return doctorIdFromRequest;
        }

        return AuthClaims.doctorId(auth)
                .orElseThrow(() -> new AccessDeniedException("doctorId mancante e claim did non presente nel token."));
    }
}
