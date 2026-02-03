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
 * Consumer Kafka per eventi di richiesta email di attivazione.
 *
 * <p>
 * Ascolta il topic {@code notifications.events} e processa gli eventi
 * {@code ACTIVATION_EMAIL_REQUESTED} per creare notifiche email
 * di attivazione account per medici e pazienti.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.notifications.activation-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ActivationEmailConsumer {

    private static final String EVENT_TYPE_ACTIVATION_EMAIL = "ACTIVATION_EMAIL_REQUESTED";

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.notifications.activation-consumer.topic:notifications.events}",
            groupId = "${sanitech.notifications.activation-consumer.group-id:svc-notifications-activation}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode payload = objectMapper.readTree(record.value());
            String eventType = payload.path("eventType").asText(null);

            if (!EVENT_TYPE_ACTIVATION_EMAIL.equals(eventType)) {
                log.debug("Evento ignorato: {}", eventType);
                return;
            }

            JsonNode eventPayload = payload.path("payload");
            processActivationEmailEvent(eventPayload);

        } catch (Exception ex) {
            log.error("Errore processamento evento da notifications.events (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    private void processActivationEmailEvent(JsonNode payload) {
        String recipientTypeStr = payload.path("recipientType").asText(null);
        String recipientId = payload.path("recipientId").asText(null);
        String email = payload.path("email").asText(null);
        String firstName = payload.path("firstName").asText(null);
        String lastName = payload.path("lastName").asText(null);

        if (email == null || email.isBlank()) {
            log.warn("Evento ACTIVATION_EMAIL_REQUESTED senza email, ignorato.");
            return;
        }

        RecipientType recipientType = parseRecipientType(recipientTypeStr);

        String subject = "Attivazione account Sanitech";
        String body = buildActivationEmailBody(firstName, lastName);

        NotificationCreateDto dto = new NotificationCreateDto(
                recipientType,
                recipientId,
                NotificationChannel.EMAIL,
                email,
                subject,
                body
        );

        notificationService.create(dto);
        log.info("Notifica di attivazione creata per {} ({})", email, recipientType);
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

    private String buildActivationEmailBody(String firstName, String lastName) {
        String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        name = name.trim();
        if (name.isEmpty()) {
            name = "Utente";
        }

        return String.format("""
                Gentile %s,

                il tuo account Sanitech è stato creato con successo.

                Le tue credenziali di accesso sono:
                - Username: la tua email
                - Password: qwerty

                Il tuo account è attualmente in attesa di attivazione.
                Riceverai una comunicazione quando l'amministratore avrà attivato il tuo profilo.

                Se non hai richiesto la creazione di questo account, ignora questa email.

                Cordiali saluti,
                Il team Sanitech
                """, name);
    }
}
