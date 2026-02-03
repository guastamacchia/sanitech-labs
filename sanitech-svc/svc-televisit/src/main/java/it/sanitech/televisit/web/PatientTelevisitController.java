package it.sanitech.televisit.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.commons.audit.Auditable;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.services.TelevisitService;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API per i pazienti: accesso alle proprie sessioni di video-visita.
 *
 * <p>
 * Espone endpoint per permettere ai pazienti di:
 * <ul>
 *   <li>Visualizzare le proprie televisite (filtrate per patientSubject)</li>
 *   <li>Ottenere il token LiveKit per partecipare alla sessione</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(AppConstants.ApiPath.PATIENT_TELEVISITS)
@PreAuthorize("hasAuthority('ROLE_PATIENT')")
@RequiredArgsConstructor
public class PatientTelevisitController {

    private final TelevisitService service;

    /**
     * Restituisce le televisite del paziente autenticato.
     * Filtra automaticamente per patientSubject = email dell'utente.
     */
    @GetMapping
    @RateLimiter(name = "televisitApi")
    public Page<TelevisitDto> getMyTelevisits(
            Authentication auth,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) TelevisitStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        Sort safeSort = SortUtils.safeSort(sort, AppConstants.SortField.TELEVISIT_SESSION_ALLOWED, AppConstants.SortField.DEFAULT_FIELD);
        Pageable pageable = PageRequest.of(page, size, safeSort);
        // Filtra automaticamente per il paziente autenticato
        return service.search(department, status, null, auth.getName(), pageable);
    }

    /**
     * Restituisce una singola televisita se appartiene al paziente autenticato.
     */
    @GetMapping("/{id}")
    public TelevisitDto getMyTelevisit(@PathVariable Long id, Authentication auth) {
        TelevisitDto televisit = service.getById(id);
        // Verifica che la televisita appartenga al paziente
        if (!televisit.patientSubject().equals(auth.getName())) {
            throw new AccessDeniedException("Non hai accesso a questa televisita.");
        }
        return televisit;
    }

    /**
     * Genera il token LiveKit per il paziente autenticato.
     * Verifica che la televisita appartenga al paziente prima di emettere il token.
     */
    @PostMapping("/{id}/token")
    @RateLimiter(name = "televisitApi")
    @Auditable(aggregateType = "TELEVISIT", eventType = "TELEVISIT_PATIENT_TOKEN_GENERATED", aggregateIdParam = "id")
    public LiveKitTokenDto getMyToken(@PathVariable Long id, Authentication auth) {
        TelevisitDto televisit = service.getById(id);
        // Verifica che la televisita appartenga al paziente
        if (!televisit.patientSubject().equals(auth.getName())) {
            throw new AccessDeniedException("Non hai accesso a questa televisita.");
        }
        return service.issuePatientToken(id);
    }
}
