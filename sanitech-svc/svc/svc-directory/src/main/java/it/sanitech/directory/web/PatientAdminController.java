package it.sanitech.directory.web;

import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API amministrativa per gestione pazienti.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_PATIENTS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PatientAdminController {

    private final PatientService patientService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto create(@Valid @RequestBody PatientCreateDto dto, Authentication auth) {
        return patientService.create(dto, auth);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PatientDto patch(@PathVariable Long id, @Valid @RequestBody PatientUpdateDto dto, Authentication auth) {
        return patientService.patch(id, dto, auth);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public org.springframework.data.domain.Page<PatientDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort,
            Authentication auth
    ) {
        // Admin controller: force admin search to bypass department filters.
        return patientService.searchAdmin(q, department, page, size, sort);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }

    @PostMapping(value = AppConstants.ApiPath.BULK, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PatientDto> bulk(@Valid @RequestBody List<PatientCreateDto> items, Authentication auth) {
        return patientService.bulkCreate(items, auth);
    }

    @GetMapping(value = AppConstants.ApiPath.EXPORT, produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department
    ) {
        byte[] csv = patientService.exportCsv(q, department);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"patients.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
