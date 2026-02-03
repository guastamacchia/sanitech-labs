package it.sanitech.consents.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.consents.repositories.entities.Consent;
import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.PrivacyConsent;
import it.sanitech.consents.repositories.ConsentRepository;
import it.sanitech.consents.repositories.PrivacyConsentRepository;
import it.sanitech.consents.services.dto.ConsentCheckResponse;
import it.sanitech.consents.services.dto.ConsentCreateDto;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.services.dto.PrivacyConsentCreateDto;
import it.sanitech.consents.services.dto.PrivacyConsentDto;
import it.sanitech.consents.services.mapper.ConsentMapper;
import it.sanitech.consents.services.mapper.PrivacyConsentMapper;
import it.sanitech.consents.utilities.AppConstants.Outbox;
import it.sanitech.outbox.core.DomainEventPublisher;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service applicativo del bounded context "Consents".
 */
@Service
@RequiredArgsConstructor
public class ConsentService {

    private static final String AGGREGATE_TYPE = "CONSENT";
    private static final String PRIVACY_AGGREGATE_TYPE = "PRIVACY_CONSENT";

    private final ConsentRepository repository;
    private final PrivacyConsentRepository privacyRepository;
    private final ConsentMapper mapper;
    private final PrivacyConsentMapper privacyMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    @Bulkhead(name = "consentsRead")
    public ConsentCheckResponse check(Long patientId, Long doctorId, ConsentScope scope) {
        var opt = repository.findByPatientIdAndDoctorIdAndScope(patientId, doctorId, scope);

        boolean allowed = opt.map(Consent::isCurrentlyGranted).orElse(false);

        return new ConsentCheckResponse(
                patientId,
                doctorId,
                scope,
                allowed,
                opt.map(Consent::getStatus).orElse(null),
                opt.map(Consent::getExpiresAt).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    @Bulkhead(name = "consentsRead")
    public List<ConsentDto> listForPatient(Long patientId) {
        return repository.findByPatientIdOrderByUpdatedAtDesc(patientId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public ConsentDto grantForPatient(Long patientId, ConsentCreateDto dto, Authentication auth) {
        ConsentScope scope = dto.scope();
        Long doctorId = dto.doctorId();

        Consent consent = repository.findByPatientIdAndDoctorIdAndScope(patientId, doctorId, scope)
                .orElseGet(() -> Consent.builder()
                        .patientId(patientId)
                        .doctorId(doctorId)
                        .scope(scope)
                        .build());

        consent.grant(dto.expiresAt());

        try {
            Consent saved = repository.save(consent);

            Map<String, Object> payload = new HashMap<>();
            payload.put("consentId", saved.getId());
            payload.put("patientId", saved.getPatientId());
            payload.put("doctorId", saved.getDoctorId());
            payload.put("scope", saved.getScope().name());
            payload.put("status", saved.getStatus().name());
            payload.put("expiresAt", saved.getExpiresAt() == null ? null : saved.getExpiresAt().toString());

            domainEventPublisher.publish(
                    AGGREGATE_TYPE,
                    String.valueOf(saved.getId()),
                    "CONSENT_GRANTED",
                    payload,
                    Outbox.TOPIC_AUDITS_EVENTS,
                    auth
            );

            return mapper.toDto(saved);

        } catch (DataIntegrityViolationException ex) {
            // In caso di race condition su vincolo unique.
            throw new IllegalStateException("Impossibile concedere il consenso: esiste già un record concorrente.", ex);
        }
    }

    @Transactional
    public void revokeForPatient(Long patientId, Long doctorId, ConsentScope scope, Authentication auth) {
        Consent consent = repository.findByPatientIdAndDoctorIdAndScope(patientId, doctorId, scope)
                .orElseThrow(() -> NotFoundException.of("Consenso per patientId=%d, doctorId=%d, scope=%s"
                        .formatted(patientId, doctorId, scope)));

        consent.revoke();
        Consent saved = repository.save(consent);

        domainEventPublisher.publish(
                AGGREGATE_TYPE,
                String.valueOf(saved.getId()),
                "CONSENT_REVOKED",
                Map.of(
                        "consentId", saved.getId(),
                        "patientId", saved.getPatientId(),
                        "doctorId", saved.getDoctorId(),
                        "scope", saved.getScope().name(),
                        "status", saved.getStatus().name()
                ),
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    @Transactional(readOnly = true)
    public ConsentDto getById(Long id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Consenso", id)));
    }

    /**
     * Restituisce gli ID dei pazienti che hanno concesso consenso TELEVISIT attivo al medico specificato.
     *
     * @param doctorId ID del medico
     * @return lista di patient ID con consenso TELEVISIT valido
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "consentsRead")
    public List<Long> getPatientIdsWithTelevisitConsent(Long doctorId) {
        return repository.findByDoctorIdAndScopeAndStatus(doctorId, ConsentScope.TELEVISIT, it.sanitech.consents.repositories.entities.ConsentStatus.GRANTED)
                .stream()
                .filter(Consent::isCurrentlyGranted)
                .map(Consent::getPatientId)
                .distinct()
                .toList();
    }

    @Transactional
    public void deleteById(Long id, Authentication auth) {
        Consent consent = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Consenso", id));
        repository.delete(consent);

        domainEventPublisher.publish(
                AGGREGATE_TYPE,
                String.valueOf(consent.getId()),
                "CONSENT_DELETED",
                Map.of(
                        "consentId", consent.getId(),
                        "patientId", consent.getPatientId(),
                        "doctorId", consent.getDoctorId(),
                        "scope", consent.getScope().name()
                ),
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }

    // ======================== Privacy Consent Methods ========================

    /**
     * Lista i consensi privacy del paziente.
     */
    @Transactional(readOnly = true)
    @Bulkhead(name = "consentsRead")
    public List<PrivacyConsentDto> listPrivacyConsentsForPatient(Long patientId) {
        return privacyRepository.findByPatientIdOrderByUpdatedAtDesc(patientId)
                .stream()
                .map(privacyMapper::toDto)
                .toList();
    }

    /**
     * Registra o aggiorna un consenso privacy per il paziente.
     */
    @Transactional
    public PrivacyConsentDto registerPrivacyConsent(Long patientId, PrivacyConsentCreateDto dto, Authentication auth) {
        PrivacyConsent consent = privacyRepository.findByPatientIdAndConsentType(patientId, dto.consentType())
                .orElseGet(() -> PrivacyConsent.builder()
                        .patientId(patientId)
                        .consentType(dto.consentType())
                        .accepted(false)
                        .build());

        consent.updateAcceptance(dto.accepted());

        try {
            PrivacyConsent saved = privacyRepository.save(consent);

            String eventType = dto.accepted() ? "PRIVACY_CONSENT_ACCEPTED" : "PRIVACY_CONSENT_DECLINED";
            domainEventPublisher.publish(
                    PRIVACY_AGGREGATE_TYPE,
                    String.valueOf(saved.getId()),
                    eventType,
                    Map.of(
                            "consentId", saved.getId(),
                            "patientId", saved.getPatientId(),
                            "consentType", saved.getConsentType().name(),
                            "accepted", saved.isAccepted()
                    ),
                    Outbox.TOPIC_AUDITS_EVENTS,
                    auth
            );

            return privacyMapper.toDto(saved);

        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Impossibile registrare il consenso privacy: esiste già un record concorrente.", ex);
        }
    }

    /**
     * Elimina un consenso privacy per ID.
     */
    @Transactional
    public void deletePrivacyConsentById(Long id, Authentication auth) {
        PrivacyConsent consent = privacyRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Consenso privacy", id));
        privacyRepository.delete(consent);

        domainEventPublisher.publish(
                PRIVACY_AGGREGATE_TYPE,
                String.valueOf(consent.getId()),
                "PRIVACY_CONSENT_DELETED",
                Map.of(
                        "consentId", consent.getId(),
                        "patientId", consent.getPatientId(),
                        "consentType", consent.getConsentType().name()
                ),
                Outbox.TOPIC_AUDITS_EVENTS,
                auth
        );
    }
}
