package it.sanitech.payments.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.payments.properties.PaymentWebhookProperties;
import it.sanitech.payments.services.PaymentOrderService;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.services.dto.webhook.PaymentWebhookDto;
import it.sanitech.payments.utilities.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoint webhook per notifiche da provider esterni.
 *
 * <p>
 * È protetto tramite header {@code X-Webhook-Secret} confrontato con proprietà applicativa.
 * </p>
 */
@RestController
@RequestMapping(AppConstants.Api.WEBHOOK_BASE + AppConstants.Api.PAYMENTS)
public class PaymentsWebhookController {

    private final PaymentOrderService service;
    private final PaymentWebhookProperties webhookProperties;

    public PaymentsWebhookController(PaymentOrderService service, PaymentWebhookProperties webhookProperties) {
        this.service = service;
        this.webhookProperties = webhookProperties;
    }

    @PostMapping("/provider")
    @Auditable(aggregateType = "PAYMENT", eventType = "PAYMENT_WEBHOOK_RECEIVED", aggregateIdSpel = "id")
    public PaymentOrderDto providerUpdate(@Valid @RequestBody PaymentWebhookDto dto,
                                          @RequestHeader(value = AppConstants.Headers.X_WEBHOOK_SECRET, required = false) String secret) {
        if (secret == null || !secret.equals(webhookProperties.getSecret())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook non autorizzato: secret non valido.");
        }

        return service.updateFromWebhook(dto.provider(), dto.providerReference(), dto.status());
    }
}
