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
        return doctorService.search(q, department, specialization, page, size, sort);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto get(@PathVariable Long id) {
        return doctorService.get(id);
    }
}
