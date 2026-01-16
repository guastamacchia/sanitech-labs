package it.sanitech.audit.web;

import it.sanitech.audit.services.AuditService;
import it.sanitech.audit.services.dto.AuditEventCreateDto;
import it.sanitech.audit.services.dto.AuditEventDto;
import it.sanitech.audit.utilities.AppConstants;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * API per la registrazione e consultazione degli eventi di audit.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstants.ApiPath.API_BASE)
public class AuditController {

    private final AuditService service;

    /**
     * Registra un evento audit (tipicamente chiamata server-to-server).
     */
    @PostMapping(AppConstants.ApiPath.AUDIT_EVENTS)
    @RateLimiter(name = "auditApi")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_audit.write')")
    public AuditEventDto record(@RequestBody @Valid AuditEventCreateDto dto, Authentication auth, HttpServletRequest request) {
        return service.recordFromApi(dto, auth, clientIp(request));
    }

    /**
     * Ricerca paginata (admin / audit.read).
     */
    @GetMapping(AppConstants.ApiPath.AUDIT_EVENTS)
    @RateLimiter(name = "auditApi")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_audit.read')")
    public Page<AuditEventDto> search(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable
    ) {
        return service.search(actorId, action, resourceType, resourceId, outcome, from, to, pageable);
    }

    /**
     * Dettaglio singolo evento audit.
     */
    @GetMapping(AppConstants.ApiPath.AUDIT_EVENTS + "/{id}")
    @RateLimiter(name = "auditApi")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SCOPE_audit.read')")
    public AuditEventDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    private static String clientIp(HttpServletRequest request) {
        // In presenza di gateway/ingress, usare X-Forwarded-For.
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
