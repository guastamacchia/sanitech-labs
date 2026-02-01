package it.sanitech.payments.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà per la protezione degli endpoint webhook (integrazione provider).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "sanitech.payments.webhook")
public class PaymentWebhookProperties {

    /**
     * Secret condiviso: deve essere inviato dal chiamante (provider) nell'header {@code X-Webhook-Secret}.
     */
    private String secret = "change-me";
}
