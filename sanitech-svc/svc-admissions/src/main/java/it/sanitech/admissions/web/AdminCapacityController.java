package it.sanitech.admissions.web;

import it.sanitech.admissions.services.CapacityService;
import it.sanitech.admissions.services.dto.CapacityDto;
import it.sanitech.admissions.services.dto.update.CapacityUpsertDto;
import it.sanitech.commons.audit.Auditable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints amministrativi per gestione capacità posti letto.
 */
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class AdminCapacityController {

    private final CapacityService capacityService;

    @PutMapping("/{dept}/capacity")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(aggregateType = "CAPACITY", eventType = "CAPACITY_UPDATED", aggregateIdParam = "dept")
    public CapacityDto setCapacity(@PathVariable("dept") String dept, @Valid @RequestBody CapacityUpsertDto body, Authentication auth) {
        return capacityService.upsert(dept, body.totalBeds(), auth);
    }
}
