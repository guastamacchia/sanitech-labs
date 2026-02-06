package it.sanitech.directory.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * API pubblica (autenticata) per la consultazione dei medici.
 *
 * <p>
 * Offre accesso in sola lettura alla ricerca e ai dettagli dei medici, con protezione
 * tramite rate limiting per mitigare carichi eccessivi.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.DOCTORS)
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Ricerca medici (paginata) con filtro opzionale su reparto e specializzazione.
     *
     * <p>
     * Protetta da RateLimiter per limitare abusi/traffic spikes.
     * </p>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "directoryApi")
    public Page<DoctorDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String specialization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        return doctorService.search(q, department, null, null, page, size, sort);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    /**
     * Lookup interno per nome e cognome (case-insensitive).
     * Utilizzato dal servizio notifiche per recuperare email medico nelle televisite.
     */
    @GetMapping(value = "/internal/by-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto findByName(@RequestParam String firstName, @RequestParam String lastName) {
        return doctorService.findByName(firstName, lastName).orElse(null);
    }

    /**
     * Lookup interno per email (case-insensitive).
     * Utilizzato dai microservizi produttori per arricchire i payload degli eventi.
     */
    @GetMapping(value = "/internal/by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto findByEmail(@RequestParam String email) {
        try {
            return doctorService.getByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
}
