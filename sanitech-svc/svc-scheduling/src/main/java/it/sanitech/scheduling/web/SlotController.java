package it.sanitech.scheduling.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.commons.audit.Auditable;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.SlotService;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * API pubblica per la consultazione degli slot disponibili.
 */
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    /**
     * Crea un nuovo slot. Richiede autenticazione e autorizzazione sul reparto.
     */
    @PostMapping
    @Auditable(aggregateType = "SLOT", eventType = "SLOT_CREATED", aggregateIdSpel = "id")
    public SlotDto create(@Valid @RequestBody SlotCreateDto dto, Authentication auth) {
        return slotService.createSlot(dto, auth);
    }

    /**
     * Ricerca slot disponibili (con RateLimiter per evitare abuso del listing).
     */
    @GetMapping
    @RateLimiter(name = "schedulingApi")
    public Page<SlotDto> search(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) VisitMode mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(defaultValue = "startAt,asc") String[] sort
    ) {
        return slotService.searchAvailableSlots(doctorId, department, mode, from, to, page, size, sort);
    }

    /**
     * Cancella uno slot. Medici possono cancellare i propri slot non prenotati.
     */
    @DeleteMapping("/{id}")
    @Auditable(aggregateType = "SLOT", eventType = "SLOT_CANCELLED", aggregateIdParam = "id")
    public void cancel(@PathVariable Long id, Authentication auth) {
        slotService.cancelSlot(id, auth);
    }
}
