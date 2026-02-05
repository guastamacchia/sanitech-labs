package it.sanitech.payments.consumers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.payments.properties.ServiceDefaultsProperties;
import it.sanitech.payments.repositories.ServicePerformedRepository;
import it.sanitech.payments.repositories.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Consumer Kafka per eventi di prestazioni sanitarie completate.
 *
 * <p>
 * Ascolta il topic {@code audits.events} e processa gli eventi:
 * <ul>
 *   <li>{@code ENDED} per le televisite (aggregateType=TELEVISIT_SESSION)</li>
 *   <li>{@code ADMISSION_DISCHARGED} per i ricoveri (aggregateType=ADMISSION)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Quando viene ricevuto un evento, crea automaticamente un record {@link ServicePerformed}
 * con l'importo di default configurato.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sanitech.service-events-consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceEventsConsumer {

    private static final String EVENT_TYPE_TELEVISIT_ENDED = "ENDED";
    private static final String EVENT_TYPE_ADMISSION_DISCHARGED = "ADMISSION_DISCHARGED";
    private static final String AGGREGATE_TYPE_TELEVISIT = "TELEVISIT_SESSION";
    private static final String AGGREGATE_TYPE_ADMISSION = "ADMISSION";

    private final ServicePerformedRepository repository;
    private final ServiceDefaultsProperties serviceDefaults;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${sanitech.service-events-consumer.topic:audits.events}",
            groupId = "${sanitech.service-events-consumer.group-id:svc-payments-service-events}"
    )
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            JsonNode envelope = objectMapper.readTree(record.value());

            String eventType = getTextOrNull(envelope, "eventType");
            String aggregateType = getTextOrNull(envelope, "aggregateType");

            if (aggregateType == null || eventType == null) {
                return;
            }

            // Processa evento televisita completata
            if (AGGREGATE_TYPE_TELEVISIT.equals(aggregateType) && EVENT_TYPE_TELEVISIT_ENDED.equals(eventType)) {
                processTelevisitEnded(envelope);
                return;
            }

            // Processa evento ricovero dimesso
            if (AGGREGATE_TYPE_ADMISSION.equals(aggregateType) && EVENT_TYPE_ADMISSION_DISCHARGED.equals(eventType)) {
                processAdmissionDischarged(envelope);
            }

        } catch (Exception ex) {
            log.error("Errore processamento evento da audits.events (offset={}): {}",
                    record.offset(), ex.getMessage(), ex);
        }
    }

    /**
     * Processa l'evento di una televisita completata.
     * Crea una prestazione di tipo MEDICAL_VISIT con importo default 100 EUR.
     */
    private void processTelevisitEnded(JsonNode envelope) {
        JsonNode payload = envelope.path("payload");
        Long sourceId = payload.path("id").asLong(0);

        if (sourceId == 0) {
            log.warn("Evento TELEVISIT_ENDED senza id, ignorato.");
            return;
        }

        // Verifica se esiste già una prestazione per questa televisita
        if (repository.existsBySourceTypeAndSourceId(ServiceSourceType.TELEVISIT, sourceId)) {
            log.debug("Prestazione già esistente per televisita id={}, ignorato.", sourceId);
            return;
        }

        String department = getTextOrNull(payload, "department");
        String patientSubject = getTextOrNull(payload, "patientSubject");
        String doctorSubject = getTextOrNull(payload, "doctorSubject");
        String roomName = getTextOrNull(payload, "roomName");

        // L'evento non contiene patientId/doctorId numerici, li lasciamo null
        // Il frontend dovrà fare un lookup se necessario

        ServicePerformed service = ServicePerformed.builder()
                .serviceType(ServiceType.MEDICAL_VISIT)
                .sourceType(ServiceSourceType.TELEVISIT)
                .sourceId(sourceId)
                .patientId(0L) // Non disponibile direttamente, serve lookup
                .patientSubject(patientSubject)
                .doctorName(doctorSubject) // Salviamo il subject, il frontend risolverà il nome
                .departmentCode(department)
                .description("Visita medica - Televisita #" + sourceId + (roomName != null ? " (" + roomName + ")" : ""))
                .amountCents(serviceDefaults.getMedicalVisitAmountCents())
                .currency("EUR")
                .status(ServicePerformedStatus.PENDING)
                .performedAt(Instant.now())
                .createdBy("system")
                .build();

        repository.save(service);
        log.info("Prestazione creata per televisita id={}, importo={}c", sourceId, service.getAmountCents());
    }

    /**
     * Processa l'evento di un paziente dimesso.
     * Crea una prestazione di tipo HOSPITALIZATION con importo calcolato in base ai giorni.
     */
    private void processAdmissionDischarged(JsonNode envelope) {
        JsonNode payload = envelope.path("payload");
        Long sourceId = payload.path("admissionId").asLong(0);

        if (sourceId == 0) {
            log.warn("Evento ADMISSION_DISCHARGED senza admissionId, ignorato.");
            return;
        }

        // Verifica se esiste già una prestazione per questo ricovero
        if (repository.existsBySourceTypeAndSourceId(ServiceSourceType.ADMISSION, sourceId)) {
            log.debug("Prestazione già esistente per ricovero id={}, ignorato.", sourceId);
            return;
        }

        Long patientId = payload.path("patientId").asLong(0);
        Long attendingDoctorId = payload.has("attendingDoctorId") ? payload.path("attendingDoctorId").asLong() : null;
        String department = getTextOrNull(payload, "departmentCode");
        String dischargedAtStr = getTextOrNull(payload, "dischargedAt");
        String admittedAtStr = getTextOrNull(payload, "admittedAt");

        // Calcola i giorni di ricovero
        int daysCount = 1; // Minimo 1 giorno
        Instant admittedAt = null;
        Instant dischargedAt = Instant.now();

        if (dischargedAtStr != null) {
            try {
                dischargedAt = Instant.parse(dischargedAtStr);
            } catch (Exception e) {
                log.debug("Impossibile parsare dischargedAt: {}", dischargedAtStr);
            }
        }

        if (admittedAtStr != null) {
            try {
                admittedAt = Instant.parse(admittedAtStr);
                long daysBetween = Duration.between(admittedAt, dischargedAt).toDays();
                daysCount = (int) Math.max(1, daysBetween);
            } catch (Exception e) {
                log.debug("Impossibile parsare admittedAt: {}", admittedAtStr);
            }
        }

        // Calcola importo: 20 EUR/giorno
        long amountCents = daysCount * serviceDefaults.getHospitalizationDailyAmountCents();

        ServicePerformed service = ServicePerformed.builder()
                .serviceType(ServiceType.HOSPITALIZATION)
                .sourceType(ServiceSourceType.ADMISSION)
                .sourceId(sourceId)
                .patientId(patientId)
                .doctorId(attendingDoctorId)
                .departmentCode(department)
                .description("Ricovero ospedaliero - " + daysCount + " giorni" + (department != null ? " (" + department + ")" : ""))
                .amountCents(amountCents)
                .currency("EUR")
                .status(ServicePerformedStatus.PENDING)
                .performedAt(dischargedAt)
                .startedAt(admittedAt)
                .daysCount(daysCount)
                .createdBy("system")
                .build();

        repository.save(service);
        log.info("Prestazione creata per ricovero id={}, giorni={}, importo={}c", sourceId, daysCount, amountCents);
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
}
