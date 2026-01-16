package it.sanitech.admissions.web;

import it.sanitech.admissions.services.CapacityService;
import it.sanitech.admissions.services.dto.CapacityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST per consultare capacità e occupazione dei reparti.
 */
@RestController
@RequestMapping("/api/departments/capacity")
@RequiredArgsConstructor
public class CapacityController {

    private final CapacityService capacityService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public List<CapacityDto> list() {
        return capacityService.listAll();
    }

    @GetMapping("/{dept}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public CapacityDto get(@PathVariable("dept") String dept) {
        return capacityService.get(dept);
    }
}
