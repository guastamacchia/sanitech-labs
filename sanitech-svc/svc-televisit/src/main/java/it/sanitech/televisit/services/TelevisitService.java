package it.sanitech.televisit.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.televisit.clients.DirectoryClient;
import it.sanitech.televisit.repositories.TelevisitSessionRepository;
import it.sanitech.televisit.repositories.entities.TelevisitSession;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.repositories.spec.TelevisitSpecifications;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.services.dto.create.TelevisitCreateDto;
import it.sanitech.televisit.services.livekit.LiveKitRoomService;
import it.sanitech.televisit.services.livekit.LiveKitTokenService;
import it.sanitech.televisit.services.mapper.TelevisitMapper;
import it.sanitech.televisit.utilities.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service applicativo per la gestione delle sessioni di video-visita.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelevisitService {

    private final TelevisitSessionRepository repo;
    private final TelevisitMapper mapper;
    private final DomainEventPublisher events;
    private final DeptGuard deptGuard;
    private final LiveKitRoomService roomService;
    private final LiveKitTokenService tokenService;
    private final DirectoryClient directoryClient;

    @Transactional
    public TelevisitDto create(TelevisitCreateDto dto, Authentication auth) {
        String roomName = generateRoomName();

        TelevisitSession entity = TelevisitSession.builder()
                .roomName(roomName)
                .department(dto.department().trim().toUpperCase())
                .doctorSubject(dto.doctorSubject().trim())
                .patientSubject(dto.patientSubject().trim())
                .scheduledAt(dto.scheduledAt())
                .status(TelevisitStatus.CREATED)
                .build();

        repo.save(entity);

        // Best-effort: crea la room su LiveKit (in alcuni setup la room nasce al primo join).
        roomService.ensureRoomExists(roomName);

        events.publish(
                AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION,
                String.valueOf(entity.getId()),
                AppConstants.Outbox.EventType.CREATED,
                Map.of(
                        "id", entity.getId(),
                        "roomName", entity.getRoomName(),
                        "department", entity.getDepartment(),
                        "doctorSubject", entity.getDoctorSubject(),
                        "patientSubject", entity.getPatientSubject(),
                        "scheduledAt", entity.getScheduledAt().toString(),
                        "status", entity.getStatus().name()
                ),
                AppConstants.Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );

        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public TelevisitDto getById(Long id) {
        return mapper.toDto(repo.findById(id).orElseThrow(() -> NotFoundException.of("TelevisitSession", id)));
    }

    @Transactional(readOnly = true)
    public Page<TelevisitDto> search(String department, TelevisitStatus status, String doctorSubject, String patientSubject, Pageable pageable) {
        return repo.findAll(TelevisitSpecifications.filter(department, status, doctorSubject, patientSubject), pageable)
                .map(mapper::toDto);
    }

    /**
     * Token per il medico autenticato.
     *
     * <p>Controlli:
     * <ul>
     *   <li>ABAC reparto: deve avere {@code DEPT_<dept>} o {@code ROLE_ADMIN}</li>
     *   <li>Se non admin, il subject del token deve coincidere con {@code doctorSubject}</li>
     * </ul>
     * </p>
     */
    @Transactional(readOnly = true)
    public LiveKitTokenDto issueDoctorToken(Long sessionId, Authentication auth) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));

        deptGuard.checkCanManage(s.getDepartment(), auth);

        if (!SecurityUtils.isAdmin(auth) && (auth == null || !s.getDoctorSubject().equals(auth.getName()))) {
            throw new AccessDeniedException("Token medico non consentito: utente non associato alla sessione.");
        }

        return tokenService.createRoomJoinToken(s.getRoomName(), auth.getName(), "doctor");
    }

    /**
     * Token paziente: esposto in endpoint admin per evitare escalation in assenza di mapping user↔patient affidabile.
     */
    @Transactional(readOnly = true)
    public LiveKitTokenDto issuePatientToken(Long sessionId) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));
        return tokenService.createRoomJoinToken(s.getRoomName(), s.getPatientSubject(), "patient");
    }

    @Transactional
    public TelevisitDto start(Long sessionId, Authentication auth) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));
        deptGuard.checkCanManage(s.getDepartment(), auth);

        if (s.getStatus() == TelevisitStatus.ENDED || s.getStatus() == TelevisitStatus.CANCELED) {
            throw new IllegalArgumentException("La sessione non può essere avviata nello stato: " + s.getStatus());
        }

        s.markActive();

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.STARTED, Map.of(
                "id", s.getId(),
                "roomName", s.getRoomName(),
                "status", s.getStatus().name()
        ), AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        return mapper.toDto(s);
    }

    @Transactional
    public TelevisitDto end(Long sessionId, Authentication auth) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));
        deptGuard.checkCanManage(s.getDepartment(), auth);

        if (s.getStatus() != TelevisitStatus.ACTIVE) {
            throw new IllegalArgumentException("La sessione può essere chiusa solo se ACTIVE.");
        }

        s.markEnded();

        // Arricchimento dati anagrafici da svc-directory
        DirectoryClient.PersonInfo patientInfo = directoryClient.findPatientByEmail(s.getPatientSubject());
        DirectoryClient.PersonInfo doctorInfo = directoryClient.findDoctorByEmail(s.getDoctorSubject());

        Long patientId = patientInfo != null ? patientInfo.id() : 0L;
        String patientName = patientInfo != null ? patientInfo.fullName() : s.getPatientSubject();
        String patientEmail = patientInfo != null ? patientInfo.email() : s.getPatientSubject();
        Long doctorId = doctorInfo != null ? doctorInfo.id() : null;
        String doctorName = doctorInfo != null ? doctorInfo.fullName() : s.getDoctorSubject();
        String doctorEmail = doctorInfo != null ? doctorInfo.email() : s.getDoctorSubject();

        // 1. Evento audit (payload minimale, invariato)
        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.ENDED, Map.of(
                "id", s.getId(),
                "roomName", s.getRoomName(),
                "department", s.getDepartment(),
                "doctorSubject", s.getDoctorSubject(),
                "patientSubject", s.getPatientSubject(),
                "status", s.getStatus().name()
        ), AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        // 2. Evento payments (payload arricchito per fatturazione)
        Map<String, Object> paymentsPayload = new HashMap<>();
        paymentsPayload.put("sourceId", s.getId());
        paymentsPayload.put("sourceType", "TELEVISIT");
        paymentsPayload.put("roomName", s.getRoomName());
        paymentsPayload.put("department", s.getDepartment());
        paymentsPayload.put("patientId", patientId);
        paymentsPayload.put("patientSubject", s.getPatientSubject());
        paymentsPayload.put("patientName", patientName);
        paymentsPayload.put("patientEmail", patientEmail);
        paymentsPayload.put("doctorId", doctorId);
        paymentsPayload.put("doctorName", doctorName);

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.ENDED,
                paymentsPayload, AppConstants.Outbox.TOPIC_PAYMENTS_EVENTS, auth);

        // 3. Evento notifications (email a medico e paziente)
        Map<String, Object> notificationsPayload = new HashMap<>();
        notificationsPayload.put("notificationType", "TELEVISIT_ENDED");
        notificationsPayload.put("sourceId", s.getId());
        notificationsPayload.put("roomName", s.getRoomName());
        notificationsPayload.put("department", s.getDepartment());
        notificationsPayload.put("patientName", patientName);
        notificationsPayload.put("patientEmail", patientEmail);
        notificationsPayload.put("doctorName", doctorName);
        notificationsPayload.put("doctorEmail", doctorEmail);

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.ENDED,
                notificationsPayload, AppConstants.Outbox.TOPIC_NOTIFICATIONS_EVENTS, auth);

        return mapper.toDto(s);
    }

    @Transactional
    public TelevisitDto cancel(Long sessionId, Authentication auth) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));
        deptGuard.checkCanManage(s.getDepartment(), auth);

        if (s.getStatus() == TelevisitStatus.ENDED) {
            throw new IllegalArgumentException("La sessione è già terminata.");
        }

        s.markCanceled();

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.CANCELED, Map.of(
                "id", s.getId(),
                "roomName", s.getRoomName(),
                "status", s.getStatus().name()
        ), AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);

        return mapper.toDto(s);
    }

    /**
     * Elimina definitivamente una sessione televisita.
     * Con force=false, solo sessioni in stato CREATED, SCHEDULED o CANCELED possono essere eliminate.
     * Con force=true, elimina la sessione indipendentemente dallo stato.
     */
    @Transactional
    public void delete(Long sessionId, Authentication auth, boolean force) {
        TelevisitSession s = repo.findById(sessionId).orElseThrow(() -> NotFoundException.of("TelevisitSession", sessionId));
        deptGuard.checkCanManage(s.getDepartment(), auth);

        if (!force && (s.getStatus() == TelevisitStatus.ACTIVE || s.getStatus() == TelevisitStatus.ENDED)) {
            throw new IllegalArgumentException("La sessione in stato " + s.getStatus() + " non può essere eliminata.");
        }

        repo.delete(s);

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(sessionId), AppConstants.Outbox.EventType.DELETED, Map.of(
                "id", sessionId,
                "roomName", s.getRoomName(),
                "status", "DELETED"
        ), AppConstants.Outbox.TOPIC_AUDITS_EVENTS, auth);
    }

    private String generateRoomName() {
        // prefisso breve + UUID: evita collisioni e rende il nome non facilmente enumerabile.
        return "tv-" + UUID.randomUUID();
    }
}
