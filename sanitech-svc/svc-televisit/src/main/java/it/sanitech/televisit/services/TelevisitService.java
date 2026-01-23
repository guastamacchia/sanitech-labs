package it.sanitech.televisit.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.outbox.core.DomainEventPublisher;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Service applicativo per la gestione delle sessioni di video-visita.
 */
@Service
@RequiredArgsConstructor
public class TelevisitService {

    private final TelevisitSessionRepository repo;
    private final TelevisitMapper mapper;
    private final DomainEventPublisher events;
    private final DeptGuard deptGuard;
    private final LiveKitRoomService roomService;
    private final LiveKitTokenService tokenService;

    @Transactional
    public TelevisitDto create(TelevisitCreateDto dto) {
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
                )
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
        ));

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

        events.publish(AppConstants.Outbox.AGGREGATE_TELEVISIT_SESSION, String.valueOf(s.getId()), AppConstants.Outbox.EventType.ENDED, Map.of(
                "id", s.getId(),
                "roomName", s.getRoomName(),
                "status", s.getStatus().name()
        ));

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
        ));

        return mapper.toDto(s);
    }

    private String generateRoomName() {
        // prefisso breve + UUID: evita collisioni e rende il nome non facilmente enumerabile.
        return "tv-" + UUID.randomUUID();
    }
}
