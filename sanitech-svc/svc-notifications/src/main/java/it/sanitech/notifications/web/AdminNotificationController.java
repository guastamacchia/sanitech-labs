package it.sanitech.notifications.web;

import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import it.sanitech.notifications.utilities.AppConstants;
import it.sanitech.notifications.utilities.SortUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * Lista tutte le notifiche del sistema con filtro opzionale per tipo destinatario.
     */
    @GetMapping
    public Page<NotificationDto> listAll(
            @RequestParam(required = false) RecipientType recipientType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                SortUtils.safeSort(
                        sort,
                        AppConstants.Notifications.SORT_CREATED_AT,
                        AppConstants.Notifications.SORT_ID,
                        AppConstants.Notifications.SORT_CREATED_AT,
                        AppConstants.Notifications.SORT_STATUS
                )
        );
        return service.listAll(recipientType, pageable);
    }

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
