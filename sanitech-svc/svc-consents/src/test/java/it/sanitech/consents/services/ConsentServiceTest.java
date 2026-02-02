package it.sanitech.consents.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.consents.repositories.ConsentRepository;
import it.sanitech.consents.repositories.PrivacyConsentRepository;
import it.sanitech.consents.repositories.entities.Consent;
import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.ConsentStatus;
import it.sanitech.consents.services.dto.ConsentCheckResponse;
import it.sanitech.consents.services.dto.ConsentCreateDto;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.services.mapper.ConsentMapper;
import it.sanitech.consents.services.mapper.PrivacyConsentMapper;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ConsentServiceTest {

    @Test
    void checkReturnsAllowedWhenConsentGranted() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        Consent consent = Consent.builder()
                .id(10L)
                .patientId(1L)
                .doctorId(2L)
                .scope(ConsentScope.DOCS)
                .status(ConsentStatus.GRANTED)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(repository.findByPatientIdAndDoctorIdAndScope(1L, 2L, ConsentScope.DOCS))
                .thenReturn(Optional.of(consent));

        ConsentCheckResponse response = service.check(1L, 2L, ConsentScope.DOCS);

        assertThat(response.allowed()).isTrue();
        assertThat(response.status()).isEqualTo(ConsentStatus.GRANTED);
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    void listForPatientMapsDtos() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        Consent consent = Consent.builder()
                .id(3L)
                .patientId(9L)
                .doctorId(7L)
                .scope(ConsentScope.RECORDS)
                .status(ConsentStatus.GRANTED)
                .build();

        ConsentDto dto = new ConsentDto(
                3L,
                9L,
                7L,
                ConsentScope.RECORDS,
                ConsentStatus.GRANTED,
                Instant.now(),
                null,
                null,
                Instant.now()
        );

        when(repository.findByPatientIdOrderByUpdatedAtDesc(9L)).thenReturn(List.of(consent));
        when(mapper.toDto(consent)).thenReturn(dto);

        List<ConsentDto> result = service.listForPatient(9L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
    }

    @Test
    void grantForPatientPublishesEvent() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        Instant expiresAt = Instant.now().plusSeconds(3600);
        ConsentCreateDto dto = new ConsentCreateDto(11L, ConsentScope.PRESCRIPTIONS, expiresAt);

        when(repository.findByPatientIdAndDoctorIdAndScope(5L, 11L, ConsentScope.PRESCRIPTIONS))
                .thenReturn(Optional.empty());
        when(repository.save(any(Consent.class))).thenAnswer(invocation -> {
            Consent saved = invocation.getArgument(0);
            return saved.toBuilder().id(22L).build();
        });
        when(mapper.toDto(any(Consent.class))).thenAnswer(invocation -> {
            Consent saved = invocation.getArgument(0);
            return new ConsentDto(
                    saved.getId(),
                    saved.getPatientId(),
                    saved.getDoctorId(),
                    saved.getScope(),
                    saved.getStatus(),
                    saved.getGrantedAt(),
                    saved.getRevokedAt(),
                    saved.getExpiresAt(),
                    saved.getUpdatedAt()
            );
        });

        ConsentDto result = service.grantForPatient(5L, dto);

        assertThat(result.id()).isEqualTo(22L);
        assertThat(result.status()).isEqualTo(ConsentStatus.GRANTED);

        ArgumentCaptor<Consent> consentCaptor = ArgumentCaptor.forClass(Consent.class);
        verify(repository).save(consentCaptor.capture());
        assertThat(consentCaptor.getValue().getGrantedAt()).isNotNull();

        verify(publisher).publish(eq("CONSENT"), eq("22"), eq("CONSENT_GRANTED"), any(), eq("audits.events"));
    }

    @Test
    void revokeForPatientUpdatesStatusAndPublishesEvent() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        Consent consent = Consent.builder()
                .id(31L)
                .patientId(5L)
                .doctorId(7L)
                .scope(ConsentScope.RECORDS)
                .status(ConsentStatus.GRANTED)
                .build();

        when(repository.findByPatientIdAndDoctorIdAndScope(5L, 7L, ConsentScope.RECORDS))
                .thenReturn(Optional.of(consent));
        when(repository.save(any(Consent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.revokeForPatient(5L, 7L, ConsentScope.RECORDS);

        ArgumentCaptor<Consent> consentCaptor = ArgumentCaptor.forClass(Consent.class);
        verify(repository).save(consentCaptor.capture());
        assertThat(consentCaptor.getValue().getStatus()).isEqualTo(ConsentStatus.REVOKED);

        verify(publisher).publish(eq("CONSENT"), eq("31"), eq("CONSENT_REVOKED"), any(), eq("audits.events"));
    }

    @Test
    void getByIdThrowsWhenMissing() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        when(repository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(44L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteByIdDeletesAndPublishesEvent() {
        ConsentRepository repository = Mockito.mock(ConsentRepository.class);
        PrivacyConsentRepository privacyRepository = Mockito.mock(PrivacyConsentRepository.class);
        ConsentMapper mapper = Mockito.mock(ConsentMapper.class);
        PrivacyConsentMapper privacyMapper = Mockito.mock(PrivacyConsentMapper.class);
        DomainEventPublisher publisher = Mockito.mock(DomainEventPublisher.class);

        ConsentService service = new ConsentService(repository, privacyRepository, mapper, privacyMapper, publisher);

        Consent consent = Consent.builder()
                .id(55L)
                .patientId(8L)
                .doctorId(9L)
                .scope(ConsentScope.DOCS)
                .status(ConsentStatus.REVOKED)
                .build();

        when(repository.findById(55L)).thenReturn(Optional.of(consent));

        service.deleteById(55L);

        verify(repository).delete(consent);
        verify(publisher).publish(eq("CONSENT"), eq("55"), eq("CONSENT_DELETED"), any(), eq("audits.events"));
    }
}
