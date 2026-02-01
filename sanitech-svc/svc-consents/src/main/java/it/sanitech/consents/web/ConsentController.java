package it.sanitech.consents.web;

import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.security.AuthClaims;
import it.sanitech.consents.services.ConsentService;
import it.sanitech.consents.services.dto.ConsentCheckResponse;
import it.sanitech.consents.services.dto.ConsentCreateDto;
import it.sanitech.consents.services.dto.ConsentDto;
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
    public PrivacyConsentDto registerPrivacyConsent(@RequestBody @Valid PrivacyConsentCreateDto dto, Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.registerPrivacyConsent(patientId, dto);
    }

    /**
     * Elimina un consenso privacy per ID.
     */
    @DeleteMapping("/me/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public void deletePrivacyConsent(@PathVariable Long id, Authentication auth) {
        requirePatientId(auth);
        service.deletePrivacyConsentById(id);
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
    public ConsentDto grantDoctorConsent(@RequestBody @Valid ConsentCreateDto dto, Authentication auth) {
        Long patientId = requirePatientId(auth);
        return service.grantForPatient(patientId, dto);
    }

    /**
     * Revoca un consenso (paziente → medico) per l'utente paziente autenticato.
     */
    @DeleteMapping("/me/doctors/{doctorId}/{scope}")
    @PreAuthorize("hasRole('PATIENT')")
    public void revokeDoctorConsent(@PathVariable Long doctorId, @PathVariable ConsentScope scope, Authentication auth) {
        Long patientId = requirePatientId(auth);
        service.revokeForPatient(patientId, doctorId, scope);
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
