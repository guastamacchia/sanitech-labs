package it.sanitech.payments.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import it.sanitech.commons.audit.Auditable;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.payments.services.PaymentOrderService;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.create.PaymentCreateDto;
import it.sanitech.payments.utilities.AppConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * API "utente" per consultare e creare pagamenti.
 *
 * <p>
 * Un PATIENT vede solo i propri ordini; un ADMIN vede tutti.
 * </p>
 */
@RestController
@RequestMapping(AppConstants.Api.API_BASE + AppConstants.Api.PAYMENTS)
public class PaymentsController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "updatedAt", "status", "amountCents", "appointmentId"
    );

    private final PaymentOrderService service;

    public PaymentsController(PaymentOrderService service) {
        this.service = service;
    }

    /**
     * Lista pagamenti dell'utente corrente.
     */
    @GetMapping
    @RateLimiter(name = "paymentsApi")
    public Page<PaymentOrderDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false, name = "sort") String[] sort,
            Authentication auth
    ) {
        Sort sortSpec = SortUtils.safeSort(sort, ALLOWED_SORT_FIELDS, AppConstants.Sort.DEFAULT_FIELD);
        PageRequest pr = PageRequest.of(page, size, sortSpec);
        return service.listForCurrentUser(pr, auth);
    }

    @GetMapping("/{id}")
    public PaymentOrderDto get(@PathVariable long id, Authentication auth) {
        return service.getById(id, auth);
    }

    /**
     * Crea un pagamento per il paziente corrente.
     *
     * <p>
     * Supporta idempotency tramite header {@code X-Idempotency-Key}.
     * </p>
     */
    @PostMapping
    @Auditable(aggregateType = "PAYMENT", eventType = "PAYMENT_INITIATED", aggregateIdSpel = "id")
    public PaymentOrderDto create(@Valid @RequestBody PaymentCreateDto dto,
                                  @RequestHeader(value = AppConstants.Headers.X_IDEMPOTENCY_KEY, required = false) String idempotencyKey,
                                  Authentication auth) {
        return service.createForCurrentPatient(dto, idempotencyKey, auth);
    }
}
