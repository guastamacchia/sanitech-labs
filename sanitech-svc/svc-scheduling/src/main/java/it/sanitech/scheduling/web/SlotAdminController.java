package it.sanitech.scheduling.web;

import it.sanitech.scheduling.services.SlotService;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API amministrativa per la gestione degli slot.
 */
@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SlotAdminController {

    private final SlotService slotService;

    @PostMapping
    public SlotDto create(@Valid @RequestBody SlotCreateDto dto, Authentication auth) {
        return slotService.createSlot(dto, auth);
    }

    @PostMapping("/_bulk")
    public List<SlotDto> bulk(@Valid @RequestBody List<SlotCreateDto> dtos, Authentication auth) {
        return slotService.createSlotsBulk(dtos, auth);
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable Long id, Authentication auth) {
        slotService.cancelSlot(id, auth);
    }
}
