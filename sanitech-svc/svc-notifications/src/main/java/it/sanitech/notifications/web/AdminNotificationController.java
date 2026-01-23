package it.sanitech.notifications.web;

import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API amministrative per gestione notifiche.
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminNotificationController {

    private final NotificationService service;

    @PostMapping
    public NotificationDto create(@Valid @RequestBody NotificationCreateDto dto) {
        return service.create(dto);
    }

    @PostMapping("/_bulk")
    public List<NotificationDto> bulkCreate(@Valid @RequestBody List<NotificationCreateDto> dtos) {
        return service.bulkCreate(dtos);
    }

    @GetMapping("/{id}")
    public NotificationDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
