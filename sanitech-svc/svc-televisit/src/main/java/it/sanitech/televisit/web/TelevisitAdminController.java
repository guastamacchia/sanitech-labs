package it.sanitech.televisit.web;

import it.sanitech.televisit.services.TelevisitService;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.services.dto.create.TelevisitCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrativa per la gestione delle sessioni di video-visita.
 */
@RestController
@RequestMapping("/api/admin/televisits")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TelevisitAdminController {

    private final TelevisitService service;

    @PostMapping
    public TelevisitDto create(@Valid @RequestBody TelevisitCreateDto dto, Authentication auth) {
        return service.create(dto, auth);
    }

    @PostMapping("/{id}/token/patient")
    public LiveKitTokenDto patientToken(@PathVariable Long id) {
        return service.issuePatientToken(id);
    }
}
