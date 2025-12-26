package it.sanitech.directory.web;

import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
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
 * API amministrativa per gestione medici.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.ADMIN_DOCTORS)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DoctorAdminController {

    private final DoctorService doctorService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto create(@Valid @RequestBody DoctorCreateDto dto, Authentication auth) {
        return doctorService.create(dto, auth);
    }

    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDto patch(@PathVariable Long id, @Valid @RequestBody DoctorUpdateDto dto, Authentication auth) {
        return doctorService.patch(id, dto, auth);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }

    @PostMapping(value = AppConstants.ApiPath.BULK, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorDto> bulk(@Valid @RequestBody List<DoctorCreateDto> items, Authentication auth) {
        return doctorService.bulkCreate(items, auth);
    }

    @GetMapping(value = AppConstants.ApiPath.EXPORT, produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String specialization
    ) {
        byte[] csv = doctorService.exportCsv(q, department, specialization);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doctors.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
