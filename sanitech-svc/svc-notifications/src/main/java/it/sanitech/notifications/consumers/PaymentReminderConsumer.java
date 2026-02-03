package it.sanitech.notifications.consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Consumer Kafka per eventi di sollecito pagamento.
 *
 * <p>
 * Ascolta il topic {@code notifications.events} e processa gli eventi
 * {@code PAYMENT_REMINDER_REQUESTED} per creare notifiche email
 * di sollecito pagamento ai pazienti.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.notifications.payment-reminder-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentReminderConsumer {

    private static final String EVENT_TYPE_PAYMENT_REMINDER = "PAYMENT_REMINDER_REQUESTED";

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.notifications.payment-reminder-consumer.topic:notifications.events}",
            groupId = "${sanitech.notifications.payment-reminder-consumer.group-id:svc-notifications-payment-reminder}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode payload = objectMapper.readTree(record.value());
            String eventType = payload.path("eventType").asText(null);

            if (!EVENT_TYPE_PAYMENT_REMINDER.equals(eventType)) {
                log.debug("Evento ignorato: {}", eventType);
                return;
            }

            JsonNode eventPayload = payload.path("payload");
            processPaymentReminderEvent(eventPayload);

        } catch (Exception ex) {
            log.error("Errore processamento evento da notifications.events (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    private void processPaymentReminderEvent(JsonNode payload) {
        String recipientId = payload.path("recipientId").asText(null);
        String email = payload.path("email").asText(null);
        String patientName = payload.path("patientName").asText(null);
        Long paymentId = payload.path("paymentId").asLong(0);
        long amountCents = payload.path("amountCents").asLong(0);
        String currency = payload.path("currency").asText("EUR");
        String description = payload.path("description").asText(null);

        if (email == null || email.isBlank()) {
            log.warn("Evento PAYMENT_REMINDER_REQUESTED senza email, ignorato.");
            return;
        }

        String subject = "Sollecito pagamento - Sanitech";
        String body = buildPaymentReminderEmailBody(patientName, paymentId, amountCents, currency, description);

        NotificationCreateDto dto = new NotificationCreateDto(
                RecipientType.PATIENT,
                recipientId,
                NotificationChannel.EMAIL,
                email,
                subject,
                body
        );

        notificationService.create(dto);
        log.info("Notifica sollecito pagamento creata per {} (paymentId={})", email, paymentId);
    }

    private String buildPaymentReminderEmailBody(String patientName, Long paymentId, long amountCents, String currency, String description) {
        String name = patientName != null && !patientName.isBlank() ? patientName : "Gentile paziente";

        String formattedAmount = formatAmount(amountCents, currency);
        String descriptionLine = description != null && !description.isBlank()
                ? String.format("Descrizione: %s%n", description)
                : "";

        return String.format("""
                Gentile %s,

                ti ricordiamo che risulta un pagamento in sospeso per i servizi Sanitech.

                Dettagli del pagamento:
                - Riferimento: #%d
                - Importo: %s
                %s
                Ti invitiamo a procedere con il pagamento al piu' presto.

                Per qualsiasi chiarimento, non esitare a contattarci.

                Cordiali saluti,
                Il team Sanitech
                """, name, paymentId, formattedAmount, descriptionLine);
    }

    private String formatAmount(long amountCents, String currency) {
        double amount = amountCents / 100.0;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.ITALY);
        try {
            formatter.setCurrency(java.util.Currency.getInstance(currency));
        } catch (IllegalArgumentException ex) {
            log.warn("Valuta non riconosciuta '{}', uso EUR come default", currency);
            formatter.setCurrency(java.util.Currency.getInstance("EUR"));
        }
        return formatter.format(amount);
    }
}
