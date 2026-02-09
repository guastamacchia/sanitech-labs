package it.sanitech.notifications.web;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.NotificationDto;
import it.sanitech.notifications.utilities.AppConstants;
import it.sanitech.notifications.utilities.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * API REST per consultare e gestire le notifiche dell'utente autenticato.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    @RateLimiter(name = "notificationsApi")
    @Bulkhead(name = "notificationsRead", type = Bulkhead.Type.SEMAPHORE)
    public Page<NotificationDto> listMyNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String[] sort
    ) {
        RecipientType recipientType = resolveRecipientType(auth);
        String recipientId = auth == null ? null : auth.getName();

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

        return service.listForRecipient(recipientType, recipientId, pageable);
    }

    /**
     * Marca una notifica come letta.
     */
    @PatchMapping("/{id}/read")
    public NotificationDto markAsRead(Authentication auth, @PathVariable Long id) {
        return service.markAsRead(id, resolveRecipientType(auth), auth.getName());
    }

    /**
     * Archivia una notifica.
     */
    @PatchMapping("/{id}/archive")
    public NotificationDto archive(Authentication auth, @PathVariable Long id) {
        return service.archive(id, resolveRecipientType(auth), auth.getName());
    }

    /**
     * Marca tutte le notifiche non lette come lette.
     */
    @PostMapping("/read-all")
    public Map<String, Integer> markAllAsRead(Authentication auth) {
        int count = service.markAllAsRead(resolveRecipientType(auth), auth.getName());
        return Map.of("updated", count);
    }

    private RecipientType resolveRecipientType(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return RecipientType.PATIENT;
        }
        Set<String> authorities = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        if (authorities.contains(it.sanitech.commons.utilities.AppConstants.Security.ROLE_ADMIN)) {
            return RecipientType.ADMIN;
        }
        if (authorities.contains(it.sanitech.commons.utilities.AppConstants.Security.ROLE_DOCTOR)) {
            return RecipientType.DOCTOR;
        }
        return RecipientType.PATIENT;
    }
}
