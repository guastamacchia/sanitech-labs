package it.sanitech.payments.web;

import it.sanitech.payments.services.PaymentOrderService;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.create.PaymentAdminCreateDto;
import it.sanitech.payments.services.dto.update.PaymentUpdateDto;
import it.sanitech.payments.utilities.AppConstants;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API amministrative per gestione pagamenti.
 */
@RestController
@RequestMapping(AppConstants.Api.ADMIN_BASE + AppConstants.Api.PAYMENTS)
@PreAuthorize("hasRole('ADMIN')")
public class PaymentsAdminController {

    private final PaymentOrderService service;

    public PaymentsAdminController(PaymentOrderService service) {
        this.service = service;
    }

    @PostMapping
    public PaymentOrderDto create(@Valid @RequestBody PaymentAdminCreateDto dto,
                                  @RequestHeader(value = AppConstants.Headers.X_IDEMPOTENCY_KEY, required = false) String idempotencyKey,
                                  Authentication auth) {
        return service.adminCreate(dto, idempotencyKey, auth);
    }

    @PatchMapping("/{id}")
    public PaymentOrderDto patch(@PathVariable long id, @RequestBody PaymentUpdateDto dto, Authentication auth) {
        return service.adminPatch(id, dto, auth);
    }

    @PostMapping("/{id}/capture")
    public PaymentOrderDto capture(@PathVariable long id, Authentication auth) {
        return service.capture(id, auth);
    }

    @PostMapping("/{id}/fail")
    public PaymentOrderDto fail(@PathVariable long id,
                               @RequestParam(required = false) String providerReference,
                               Authentication auth) {
        return service.fail(id, providerReference, auth);
    }

    @PostMapping("/{id}/cancel")
    public PaymentOrderDto cancel(@PathVariable long id, Authentication auth) {
        return service.cancel(id, auth);
    }

    @PostMapping("/{id}/refund")
    public PaymentOrderDto refund(@PathVariable long id, Authentication auth) {
        return service.refund(id, auth);
    }

    @PostMapping("/{id}/reminder")
    public PaymentOrderDto sendReminder(@PathVariable long id, Authentication auth) {
        return service.sendReminder(id, auth);
    }
}
