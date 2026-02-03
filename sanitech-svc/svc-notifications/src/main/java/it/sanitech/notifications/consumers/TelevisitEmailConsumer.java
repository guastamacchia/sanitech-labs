package it.sanitech.notifications.consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.notifications.clients.DirectoryClient;
import it.sanitech.notifications.clients.DirectoryClient.PersonInfo;
import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.create.NotificationCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Consumer Kafka per eventi di creazione televisita.
 *
 * <p>
 * Ascolta il topic {@code audits.events} e processa gli eventi
 * {@code TELEVISIT_SESSION.CREATED} per inviare email di notifica
 * sia al medico che al paziente con il link per la videochiamata.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.notifications.televisit-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelevisitEmailConsumer {

    private static final String AGGREGATE_TYPE_TELEVISIT = "TELEVISIT_SESSION";
    private static final String EVENT_TYPE_CREATED = "CREATED";

    private final NotificationService notificationService;
    private final DirectoryClient directoryClient;
    private final ObjectMapper objectMapper;

    @Value("${sanitech.televisit.room-base-url:https://meet.sanitech.it/room/}")
    private String roomBaseUrl;

    @KafkaListener(
            topics = "${sanitech.notifications.televisit-consumer.topic:audits.events}",
            groupId = "${sanitech.notifications.televisit-consumer.group-id:svc-notifications-televisit}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = objectMapper.readTree(record.value());
            String aggregateType = event.path("aggregateType").asText(null);
            String eventType = event.path("eventType").asText(null);

            if (!AGGREGATE_TYPE_TELEVISIT.equals(aggregateType) || !EVENT_TYPE_CREATED.equals(eventType)) {
                return;
            }

            JsonNode payload = event.path("payload");
            processTelevisitCreated(payload);

        } catch (Exception ex) {
            log.error("Errore processamento evento televisita (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    private void processTelevisitCreated(JsonNode payload) {
        String roomName = payload.path("roomName").asText(null);
        String doctorFullName = payload.path("doctorSubject").asText(null);
        String patientFullName = payload.path("patientSubject").asText(null);
        String scheduledAtStr = payload.path("scheduledAt").asText(null);

        if (roomName == null || doctorFullName == null || patientFullName == null) {
            log.warn("Evento TELEVISIT_SESSION.CREATED incompleto, ignorato. roomName={}, doctor={}, patient={}",
                    roomName, doctorFullName, patientFullName);
            return;
        }

        String roomUrl = roomBaseUrl + roomName;
        String formattedDate = formatScheduledAt(scheduledAtStr);

        // Invia email al medico
        PersonInfo doctor = directoryClient.findByFullName(doctorFullName, true);
        if (doctor != null && doctor.email() != null) {
            sendDoctorEmail(doctor, patientFullName, formattedDate, roomUrl);
        } else {
            log.warn("Impossibile inviare email al medico: lookup fallito per {}", doctorFullName);
        }

        // Invia email al paziente
        PersonInfo patient = directoryClient.findByFullName(patientFullName, false);
        if (patient != null && patient.email() != null) {
            sendPatientEmail(patient, doctorFullName, formattedDate, roomUrl);
        } else {
            log.warn("Impossibile inviare email al paziente: lookup fallito per {}", patientFullName);
        }
    }

    private void sendDoctorEmail(PersonInfo doctor, String patientFullName, String scheduledAt, String roomUrl) {
        String subject = "Televisita programmata con " + patientFullName;
        String body = buildDoctorEmailBody(doctor.firstName(), patientFullName, scheduledAt, roomUrl);

        NotificationCreateDto dto = new NotificationCreateDto(
                RecipientType.DOCTOR,
                null,
                NotificationChannel.EMAIL,
                doctor.email(),
                subject,
                body
        );

        notificationService.create(dto);
        log.info("Email televisita inviata al medico {} per visita con {}", doctor.email(), patientFullName);
    }

    private void sendPatientEmail(PersonInfo patient, String doctorFullName, String scheduledAt, String roomUrl) {
        String subject = "Televisita programmata con Dr. " + doctorFullName;
        String body = buildPatientEmailBody(patient.firstName(), doctorFullName, scheduledAt, roomUrl);

        NotificationCreateDto dto = new NotificationCreateDto(
                RecipientType.PATIENT,
                null,
                NotificationChannel.EMAIL,
                patient.email(),
                subject,
                body
        );

        notificationService.create(dto);
        log.info("Email televisita inviata al paziente {} per visita con Dr. {}", patient.email(), doctorFullName);
    }

    private String buildDoctorEmailBody(String doctorFirstName, String patientFullName, String scheduledAt, String roomUrl) {
        return String.format("""
                Gentile Dr. %s,

                è stata programmata una televisita con il paziente %s.

                Data e ora: %s

                Per avviare la videochiamata, clicca sul seguente link:
                %s

                Ti consigliamo di accedere qualche minuto prima dell'orario previsto
                per verificare che audio e video funzionino correttamente.

                Cordiali saluti,
                Il team Sanitech
                """, doctorFirstName, patientFullName, scheduledAt, roomUrl);
    }

    private String buildPatientEmailBody(String patientFirstName, String doctorFullName, String scheduledAt, String roomUrl) {
        return String.format("""
                Gentile %s,

                è stata programmata una televisita con il Dr. %s.

                Data e ora: %s

                Per collegarti alla videochiamata, clicca sul seguente link:
                %s

                Ti consigliamo di:
                - Accedere qualche minuto prima dell'orario previsto
                - Verificare che audio e video del tuo dispositivo funzionino
                - Scegliere un luogo tranquillo e ben illuminato

                Se hai difficoltà tecniche, contatta il supporto Sanitech.

                Cordiali saluti,
                Il team Sanitech
                """, patientFirstName, doctorFullName, scheduledAt, roomUrl);
    }

    private String formatScheduledAt(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) {
            return "Data non disponibile";
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm", Locale.ITALIAN);
            return odt.format(formatter);
        } catch (DateTimeParseException ex) {
            log.warn("Impossibile parsare data scheduledAt: {}", isoDateTime);
            return isoDateTime;
        }
    }
}
