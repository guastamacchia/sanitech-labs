package it.sanitech.payments.web;

import it.sanitech.payments.repositories.entities.ServicePerformedStatus;
import it.sanitech.payments.services.ServicePerformedService;
import it.sanitech.payments.services.dto.ServicePerformedDto;
import it.sanitech.payments.services.dto.ServicePerformedStatsDto;
import it.sanitech.payments.services.dto.create.ServicePerformedCreateDto;
import it.sanitech.payments.services.dto.update.ServicePerformedUpdateDto;
import it.sanitech.payments.utilities.AppConstants;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API amministrative per gestione prestazioni sanitarie.
 *
 * <p>
 * Le prestazioni vengono create automaticamente quando:
 * <ul>
 *   <li>Una televisita viene completata (visita medica - 100 EUR)</li>
 *   <li>Un paziente viene dimesso da un ricovero (20 EUR al giorno)</li>
 * </ul>
 * </p>
 *
 * <p>
 * L'amministratore può:
 * <ul>
 *   <li>Visualizzare tutte le prestazioni con filtri</li>
 *   <li>Modificare l'importo di una prestazione</li>
 *   <li>Convertire una prestazione in gratuita</li>
 *   <li>Segnare una prestazione come pagata</li>
 *   <li>Annullare o eliminare una prestazione</li>
 *   <li>Inviare solleciti di pagamento</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping(AppConstants.Api.ADMIN_BASE + AppConstants.Api.SERVICES)
@PreAuthorize("hasRole('ADMIN')")
public class ServicesAdminController {

    private final ServicePerformedService service;

    public ServicesAdminController(ServicePerformedService service) {
        this.service = service;
    }

    /**
     * Lista tutte le prestazioni con paginazione e filtri.
     *
     * @param status filtro per stato (PENDING, PAID, FREE, CANCELLED)
     * @param pageable parametri di paginazione
     */
    @GetMapping
    public Page<ServicePerformedDto> list(
            @RequestParam(required = false) ServicePerformedStatus status,
            @PageableDefault(sort = "performedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, pageable);
    }

    /**
     * Crea una nuova prestazione manuale.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicePerformedDto create(
            @Valid @RequestBody ServicePerformedCreateDto dto,
            Authentication auth) {
        return service.create(dto, auth);
    }

    /**
     * Ottiene le statistiche delle prestazioni.
     */
    @GetMapping("/stats")
    public ServicePerformedStatsDto getStats() {
        return service.getStats();
    }

    /**
     * Ottiene una singola prestazione.
     */
    @GetMapping("/{id}")
    public ServicePerformedDto getById(@PathVariable long id) {
        return service.getById(id);
    }

    /**
     * Aggiorna parzialmente una prestazione (importo, note, dati paziente).
     */
    @PatchMapping("/{id}")
    public ServicePerformedDto patch(
            @PathVariable long id,
            @Valid @RequestBody ServicePerformedUpdateDto dto,
            Authentication auth) {
        return service.patch(id, dto, auth);
    }

    /**
     * Segna una prestazione come pagata.
     */
    @PostMapping("/{id}/paid")
    public ServicePerformedDto markAsPaid(@PathVariable long id, Authentication auth) {
        return service.markAsPaid(id, auth);
    }

    /**
     * Converte una prestazione in gratuita.
     *
     * @param id ID della prestazione
     * @param reason motivo della gratuità (opzionale)
     */
    @PostMapping("/{id}/free")
    public ServicePerformedDto markAsFree(
            @PathVariable long id,
            @RequestParam(required = false) String reason,
            Authentication auth) {
        return service.markAsFree(id, reason, auth);
    }

    /**
     * Annulla una prestazione (soft delete).
     */
    @PostMapping("/{id}/cancel")
    public ServicePerformedDto cancel(@PathVariable long id, Authentication auth) {
        return service.cancel(id, auth);
    }

    /**
     * Elimina definitivamente una prestazione.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, Authentication auth) {
        service.delete(id, auth);
    }

    /**
     * Invia un sollecito di pagamento al paziente.
     */
    @PostMapping("/{id}/reminder")
    public ServicePerformedDto sendReminder(@PathVariable long id, Authentication auth) {
        return service.sendReminder(id, auth);
    }

    /**
     * Invia solleciti multipli a più pazienti.
     *
     * @param ids lista degli ID delle prestazioni
     */
    @PostMapping("/bulk-reminders")
    public Map<String, Integer> sendBulkReminders(@RequestBody List<Long> ids, Authentication auth) {
        return service.sendBulkReminders(ids, auth);
    }
}
