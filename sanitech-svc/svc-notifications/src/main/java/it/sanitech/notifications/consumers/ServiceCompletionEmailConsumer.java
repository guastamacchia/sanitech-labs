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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

/**
 * Consumer Kafka per eventi di completamento prestazioni sanitarie.
 *
 * <p>
 * Ascolta il topic {@code notifications.events} e processa gli eventi:
 * <ul>
 *   <li>{@code TELEVISIT_ENDED} — televisita completata</li>
 *   <li>{@code ADMISSION_DISCHARGED} — paziente dimesso da ricovero</li>
 *   <li>{@code APPOINTMENT_COMPLETED} — visita in presenza completata</li>
 * </ul>
 * Per ciascun evento invia due email: una al paziente e una al medico.
 * </p>
 *
 * <p>
 * I payload arrivano già arricchiti con dati anagrafici (nome, email) dai produttori,
 * che chiamano svc-directory prima di pubblicare l'evento.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.notifications.service-completion-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceCompletionEmailConsumer {

    private static final String EVENT_TYPE_TELEVISIT_ENDED = "TELEVISIT_ENDED";
    private static final String EVENT_TYPE_ADMISSION_DISCHARGED = "ADMISSION_DISCHARGED";
    private static final String EVENT_TYPE_APPOINTMENT_COMPLETED = "APPOINTMENT_COMPLETED";

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            EVENT_TYPE_TELEVISIT_ENDED,
            EVENT_TYPE_ADMISSION_DISCHARGED,
            EVENT_TYPE_APPOINTMENT_COMPLETED
    );

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.notifications.service-completion-consumer.topic:notifications.events}",
            groupId = "${sanitech.notifications.service-completion-consumer.group-id:svc-notifications-service-completion}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode envelope = objectMapper.readTree(record.value());
            String eventType = getTextOrNull(envelope, "eventType");

            if (eventType == null || !SUPPORTED_EVENT_TYPES.contains(eventType)) {
                log.debug("Evento ignorato: {}", eventType);
                return;
            }

            JsonNode payload = envelope.path("payload");

            switch (eventType) {
                case EVENT_TYPE_TELEVISIT_ENDED -> processTelevisitEnded(payload);
                case EVENT_TYPE_ADMISSION_DISCHARGED -> processAdmissionDischarged(payload);
                case EVENT_TYPE_APPOINTMENT_COMPLETED -> processAppointmentCompleted(payload);
            }

        } catch (Exception ex) {
            log.error("Errore processamento evento da notifications.events (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    // ──────────────────────────────────────────────
    // Televisita completata
    // ──────────────────────────────────────────────

    private void processTelevisitEnded(JsonNode payload) {
        String patientName = getTextOrNull(payload, "patientName");
        String patientEmail = getTextOrNull(payload, "patientEmail");
        String doctorName = getTextOrNull(payload, "doctorName");
        String doctorEmail = getTextOrNull(payload, "doctorEmail");
        String roomName = getTextOrNull(payload, "roomName");
        String department = getTextOrNull(payload, "department");

        // Email al paziente
        if (patientEmail != null && !patientEmail.isBlank()) {
            String subject = "Riepilogo televisita completata";
            String body = buildTelevisitPatientEmail(patientName, doctorName, roomName, department);

            createNotification(RecipientType.PATIENT, patientEmail, subject, body);
            log.info("Email televisita completata inviata al paziente {}", patientEmail);
        } else {
            log.warn("Evento TELEVISIT_ENDED senza email paziente, notifica paziente non inviata.");
        }

        // Email al medico
        if (doctorEmail != null && !doctorEmail.isBlank()) {
            String subject = "Televisita completata con " + (patientName != null ? patientName : "paziente");
            String body = buildTelevisitDoctorEmail(doctorName, patientName, roomName, department);

            createNotification(RecipientType.DOCTOR, doctorEmail, subject, body);
            log.info("Email televisita completata inviata al medico {}", doctorEmail);
        } else {
            log.warn("Evento TELEVISIT_ENDED senza email medico, notifica medico non inviata.");
        }
    }

    private String buildTelevisitPatientEmail(String patientName, String doctorName, String roomName, String department) {
        String name = patientName != null ? patientName : "Gentile paziente";
        String doctor = doctorName != null ? "Dr. " + doctorName : "il medico";
        String deptInfo = department != null ? " (" + department + ")" : "";

        return String.format("""
                Gentile %s,

                la tua televisita con %s%s è stata completata con successo.

                Ti ricordiamo che il riepilogo della visita sarà disponibile nella tua area personale.
                Se hai bisogno di ulteriori chiarimenti, non esitare a contattarci.

                Cordiali saluti,
                Il team Sanitech
                """, name, doctor, deptInfo);
    }

    private String buildTelevisitDoctorEmail(String doctorName, String patientName, String roomName, String department) {
        String name = doctorName != null ? doctorName : "Dottore";
        String patient = patientName != null ? patientName : "il paziente";

        return String.format("""
                Gentile Dr. %s,

                la televisita con il paziente %s è stata completata con successo.

                Il riepilogo è disponibile nel sistema.

                Cordiali saluti,
                Il team Sanitech
                """, name, patient);
    }

    // ──────────────────────────────────────────────
    // Ricovero — paziente dimesso
    // ──────────────────────────────────────────────

    private void processAdmissionDischarged(JsonNode payload) {
        String patientName = getTextOrNull(payload, "patientName");
        String patientEmail = getTextOrNull(payload, "patientEmail");
        String doctorName = getTextOrNull(payload, "doctorName");
        String doctorEmail = getTextOrNull(payload, "doctorEmail");
        String departmentCode = getTextOrNull(payload, "departmentCode");
        String admittedAtStr = getTextOrNull(payload, "admittedAt");
        String dischargedAtStr = getTextOrNull(payload, "dischargedAt");

        String admittedFormatted = formatInstant(admittedAtStr);
        String dischargedFormatted = formatInstant(dischargedAtStr);

        // Email al paziente
        if (patientEmail != null && !patientEmail.isBlank()) {
            String subject = "Dimissione completata — Sanitech";
            String body = buildAdmissionPatientEmail(patientName, doctorName, departmentCode, admittedFormatted, dischargedFormatted);

            createNotification(RecipientType.PATIENT, patientEmail, subject, body);
            log.info("Email dimissione inviata al paziente {}", patientEmail);
        } else {
            log.warn("Evento ADMISSION_DISCHARGED senza email paziente, notifica paziente non inviata.");
        }

        // Email al medico
        if (doctorEmail != null && !doctorEmail.isBlank()) {
            String subject = "Dimissione paziente " + (patientName != null ? patientName : "") + " completata";
            String body = buildAdmissionDoctorEmail(doctorName, patientName, departmentCode, admittedFormatted, dischargedFormatted);

            createNotification(RecipientType.DOCTOR, doctorEmail, subject, body);
            log.info("Email dimissione inviata al medico {}", doctorEmail);
        } else {
            log.warn("Evento ADMISSION_DISCHARGED senza email medico, notifica medico non inviata.");
        }
    }

    private String buildAdmissionPatientEmail(String patientName, String doctorName, String department,
                                               String admittedAt, String dischargedAt) {
        String name = patientName != null ? patientName : "Gentile paziente";
        String doctor = doctorName != null ? "Dr. " + doctorName : "il medico curante";
        String deptInfo = department != null ? "Reparto: " + department + "\n" : "";

        return String.format("""
                Gentile %s,

                la tua dimissione dall'ospedale è stata registrata con successo.

                Dettagli del ricovero:
                %s- Medico curante: %s
                - Data ammissione: %s
                - Data dimissione: %s

                Ti ricordiamo di seguire le indicazioni mediche ricevute alla dimissione.
                Per qualsiasi necessità, contatta il tuo medico di riferimento.

                Cordiali saluti,
                Il team Sanitech
                """, name, deptInfo, doctor, admittedAt, dischargedAt);
    }

    private String buildAdmissionDoctorEmail(String doctorName, String patientName, String department,
                                              String admittedAt, String dischargedAt) {
        String name = doctorName != null ? doctorName : "Dottore";
        String patient = patientName != null ? patientName : "il paziente";

        return String.format("""
                Gentile Dr. %s,

                la dimissione del paziente %s è stata registrata con successo.

                - Data ammissione: %s
                - Data dimissione: %s

                Il riepilogo è disponibile nel sistema.

                Cordiali saluti,
                Il team Sanitech
                """, name, patient, admittedAt, dischargedAt);
    }

    // ──────────────────────────────────────────────
    // Visita in presenza completata
    // ──────────────────────────────────────────────

    private void processAppointmentCompleted(JsonNode payload) {
        String patientName = getTextOrNull(payload, "patientName");
        String patientEmail = getTextOrNull(payload, "patientEmail");
        String doctorName = getTextOrNull(payload, "doctorName");
        String doctorEmail = getTextOrNull(payload, "doctorEmail");
        String departmentCode = getTextOrNull(payload, "departmentCode");
        String mode = getTextOrNull(payload, "mode");
        String completedAtStr = getTextOrNull(payload, "completedAt");

        String completedFormatted = formatInstant(completedAtStr);
        String modeLabel = "TELEVISIT".equals(mode) ? "Televisita" : "In presenza";

        // Email al paziente
        if (patientEmail != null && !patientEmail.isBlank()) {
            String subject = "Visita medica completata — Sanitech";
            String body = buildAppointmentPatientEmail(patientName, doctorName, departmentCode, modeLabel, completedFormatted);

            createNotification(RecipientType.PATIENT, patientEmail, subject, body);
            log.info("Email visita completata inviata al paziente {}", patientEmail);
        } else {
            log.warn("Evento APPOINTMENT_COMPLETED senza email paziente, notifica paziente non inviata.");
        }

        // Email al medico
        if (doctorEmail != null && !doctorEmail.isBlank()) {
            String subject = "Visita con " + (patientName != null ? patientName : "paziente") + " completata";
            String body = buildAppointmentDoctorEmail(doctorName, patientName, departmentCode, modeLabel, completedFormatted);

            createNotification(RecipientType.DOCTOR, doctorEmail, subject, body);
            log.info("Email visita completata inviata al medico {}", doctorEmail);
        } else {
            log.warn("Evento APPOINTMENT_COMPLETED senza email medico, notifica medico non inviata.");
        }
    }

    private String buildAppointmentPatientEmail(String patientName, String doctorName, String department,
                                                 String mode, String completedAt) {
        String name = patientName != null ? patientName : "Gentile paziente";
        String doctor = doctorName != null ? "Dr. " + doctorName : "il medico";
        String deptInfo = department != null ? " (" + department + ")" : "";

        return String.format("""
                Gentile %s,

                la tua visita medica (%s) con %s%s è stata completata con successo.

                Data completamento: %s

                Il riepilogo della visita sarà disponibile nella tua area personale.
                Per qualsiasi chiarimento, non esitare a contattarci.

                Cordiali saluti,
                Il team Sanitech
                """, name, mode, doctor, deptInfo, completedAt);
    }

    private String buildAppointmentDoctorEmail(String doctorName, String patientName, String department,
                                                String mode, String completedAt) {
        String name = doctorName != null ? doctorName : "Dottore";
        String patient = patientName != null ? patientName : "il paziente";

        return String.format("""
                Gentile Dr. %s,

                la visita medica (%s) con il paziente %s è stata completata.

                Data completamento: %s

                Il riepilogo è disponibile nel sistema.

                Cordiali saluti,
                Il team Sanitech
                """, name, mode, patient, completedAt);
    }

    // ──────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────

    private void createNotification(RecipientType recipientType, String email, String subject, String body) {
        NotificationCreateDto dto = new NotificationCreateDto(
                recipientType,
                email, // recipientId = email per semplicità
                NotificationChannel.EMAIL,
                email,
                subject,
                body
        );

        notificationService.create(dto);
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private String formatInstant(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) {
            return "Data non disponibile";
        }
        try {
            // Prova come OffsetDateTime (ISO 8601 con offset)
            OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm", Locale.ITALIAN);
            return odt.format(formatter);
        } catch (DateTimeParseException ex1) {
            try {
                // Prova come Instant (es. 2024-01-15T10:30:00Z)
                Instant instant = Instant.parse(isoDateTime);
                OffsetDateTime odt = instant.atOffset(java.time.ZoneOffset.of("+01:00"));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm", Locale.ITALIAN);
                return odt.format(formatter);
            } catch (Exception ex2) {
                log.warn("Impossibile parsare data: {}", isoDateTime);
                return isoDateTime;
            }
        }
    }
}
