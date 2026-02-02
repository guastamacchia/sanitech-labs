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

/**
 * Consumer Kafka per eventi di cambio stato account (attivazione/disattivazione).
 *
 * <p>
 * Ascolta il topic {@code notifications.events} e processa gli eventi
 * {@code ACCOUNT_ENABLED_EMAIL_REQUESTED} e {@code ACCOUNT_DISABLED_EMAIL_REQUESTED}
 * per creare notifiche email quando un amministratore attiva o disattiva
 * un account medico o paziente.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.notifications.account-status-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccountStatusEmailConsumer {

    private static final String EVENT_TYPE_ACCOUNT_ENABLED = "ACCOUNT_ENABLED_EMAIL_REQUESTED";
    private static final String EVENT_TYPE_ACCOUNT_DISABLED = "ACCOUNT_DISABLED_EMAIL_REQUESTED";

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.notifications.account-status-consumer.topic:notifications.events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode payload = objectMapper.readTree(record.value());
            String eventType = payload.path("eventType").asText(null);

            if (EVENT_TYPE_ACCOUNT_ENABLED.equals(eventType)) {
                processAccountStatusEvent(payload.path("payload"), true);
            } else if (EVENT_TYPE_ACCOUNT_DISABLED.equals(eventType)) {
                processAccountStatusEvent(payload.path("payload"), false);
            } else {
                log.debug("Evento ignorato: {}", eventType);
            }

        } catch (Exception ex) {
            log.error("Errore processamento evento da notifications.events (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    private void processAccountStatusEvent(JsonNode payload, boolean enabled) {
        String recipientTypeStr = payload.path("recipientType").asText(null);
        String recipientId = payload.path("recipientId").asText(null);
        String email = payload.path("email").asText(null);
        String firstName = payload.path("firstName").asText(null);
        String lastName = payload.path("lastName").asText(null);

        if (email == null || email.isBlank()) {
            log.warn("Evento cambio stato account senza email, ignorato.");
            return;
        }

        RecipientType recipientType = parseRecipientType(recipientTypeStr);

        String subject = enabled
                ? "Account Sanitech attivato"
                : "Account Sanitech disattivato";

        String body = enabled
                ? buildAccountEnabledEmailBody(firstName, lastName, recipientType)
                : buildAccountDisabledEmailBody(firstName, lastName, recipientType);

        NotificationCreateDto dto = new NotificationCreateDto(
                recipientType,
                recipientId,
                NotificationChannel.EMAIL,
                email,
                subject,
                body
        );

        notificationService.create(dto);
        log.info("Notifica cambio stato account ({}) creata per {} ({})",
                enabled ? "attivato" : "disattivato", email, recipientType);
    }

    private RecipientType parseRecipientType(String type) {
        if (type == null) {
            return RecipientType.PATIENT;
        }
        try {
            return RecipientType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.warn("RecipientType non riconosciuto '{}', uso default PATIENT", type);
            return RecipientType.PATIENT;
        }
    }

    private String buildAccountEnabledEmailBody(String firstName, String lastName, RecipientType recipientType) {
        String name = formatName(firstName, lastName);
        String greeting = recipientType == RecipientType.DOCTOR ? "Dott. " + name : name;

        return String.format("""
                Gentile %s,

                il tuo account Sanitech è stato attivato con successo.

                Da questo momento puoi accedere al portale utilizzando le credenziali
                che hai ricevuto al momento della registrazione.

                Se hai dimenticato la password, puoi richiedere il reset dalla pagina di login.

                Cordiali saluti,
                Il team Sanitech
                """, greeting);
    }

    private String buildAccountDisabledEmailBody(String firstName, String lastName, RecipientType recipientType) {
        String name = formatName(firstName, lastName);
        String greeting = recipientType == RecipientType.DOCTOR ? "Dott. " + name : name;

        return String.format("""
                Gentile %s,

                il tuo account Sanitech è stato disattivato.

                Non potrai più accedere al portale fino a quando l'amministratore
                non riabiliterà il tuo account.

                Se ritieni che questa disattivazione sia avvenuta per errore,
                ti preghiamo di contattare il supporto tecnico.

                Cordiali saluti,
                Il team Sanitech
                """, greeting);
    }

    private String formatName(String firstName, String lastName) {
        String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        name = name.trim();
        return name.isEmpty() ? "Utente" : name;
    }
}
