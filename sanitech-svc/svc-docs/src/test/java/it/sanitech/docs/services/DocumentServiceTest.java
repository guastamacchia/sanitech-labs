package it.sanitech.docs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.docs.integrations.consents.ConsentClient;
import it.sanitech.docs.repositories.DocumentRepository;
import it.sanitech.docs.repositories.entities.Document;
import it.sanitech.docs.services.dto.DocumentDto;
import it.sanitech.docs.services.mappers.DocumentMapper;
import it.sanitech.docs.storage.S3StorageService;
import it.sanitech.outbox.core.DomainEventPublisher;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class DocumentServiceTest {

    @Test
    void uploadStoresDocumentAndPublishesEvent() throws Exception {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "test-content".getBytes()
        );

        JwtAuthenticationToken auth = jwtAuth("admin", "ROLE_ADMIN");

        when(documents.save(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            return Document.builder()
                    .id(UUID.randomUUID())
                    .patientId(saved.getPatientId())
                    .uploadedBy(saved.getUploadedBy())
                    .departmentCode(saved.getDepartmentCode())
                    .documentType(saved.getDocumentType())
                    .fileName(saved.getFileName())
                    .contentType(saved.getContentType())
                    .sizeBytes(saved.getSizeBytes())
                    .checksumSha256(saved.getChecksumSha256())
                    .s3Key(saved.getS3Key())
                    .description(saved.getDescription())
                    .createdAt(Instant.now())
                    .build();
        });
        when(mapper.toDto(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            return new DocumentDto(
                    saved.getId(),
                    saved.getPatientId(),
                    saved.getDepartmentCode(),
                    saved.getDocumentType(),
                    saved.getFileName(),
                    saved.getContentType(),
                    saved.getSizeBytes(),
                    saved.getChecksumSha256(),
                    saved.getDescription(),
                    saved.getCreatedAt()
            );
        });

        DocumentDto result = service.upload(file, 42L, "cardio", "report", "note", auth);

        assertThat(result.patientId()).isEqualTo(42L);
        assertThat(result.documentType()).isEqualTo("REPORT");

        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documents).save(docCaptor.capture());
        assertThat(docCaptor.getValue().getDepartmentCode()).isEqualTo("CARDIO");

        verify(storage).put(any(), any(InputStream.class), eq((long) file.getBytes().length), eq("application/pdf"));
        verify(events).publish(eq("DOCUMENT"), any(), eq("DOCUMENT_UPLOADED"), any());
    }

    @Test
    void listForDoctorRequiresConsentAndDepartments() {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        JwtAuthenticationToken auth = jwtAuth("doctor", "ROLE_DOCTOR", "DEPT_CARDIO");

        Document doc = Document.builder()
                .id(UUID.randomUUID())
                .patientId(10L)
                .departmentCode("CARDIO")
                .documentType("REPORT")
                .fileName("report.pdf")
                .contentType("application/pdf")
                .sizeBytes(120L)
                .checksumSha256("hash")
                .s3Key("s3-key")
                .createdAt(Instant.now())
                .build();

        Page<Document> page = new PageImpl<>(List.of(doc), PageRequest.of(0, 10), 1);
        when(documents.findByPatientIdAndDepartmentCodeIn(eq(10L), eq(Set.of("CARDIO")), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toDto(doc)).thenReturn(new DocumentDto(
                doc.getId(),
                doc.getPatientId(),
                doc.getDepartmentCode(),
                doc.getDocumentType(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getSizeBytes(),
                doc.getChecksumSha256(),
                doc.getDescription(),
                doc.getCreatedAt()
        ));

        Page<DocumentDto> result = service.list(auth, 10L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(consentClient).assertConsentForDocs(10L, auth);
    }

    @Test
    void listForPatientUsesPidClaim() {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        JwtAuthenticationToken auth = jwtAuth("patient", "ROLE_PATIENT");

        Page<Document> page = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);
        when(documents.findByPatientId(eq(99L), any(Pageable.class))).thenReturn(page);

        Page<DocumentDto> result = service.list(auth, null, PageRequest.of(0, 5));

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void deleteRequiresAdmin() {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        JwtAuthenticationToken auth = jwtAuth("patient", "ROLE_PATIENT");

        assertThatThrownBy(() -> service.delete(UUID.randomUUID(), auth))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteRemovesDocumentAndPublishesEvent() {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        UUID docId = UUID.randomUUID();
        Document doc = Document.builder()
                .id(docId)
                .patientId(10L)
                .departmentCode("CARDIO")
                .documentType("REPORT")
                .fileName("report.pdf")
                .contentType("application/pdf")
                .sizeBytes(120L)
                .checksumSha256("hash")
                .s3Key("s3-key")
                .createdAt(Instant.now())
                .build();

        when(documents.findById(docId)).thenReturn(Optional.of(doc));
        doNothing().when(storage).delete("s3-key");

        JwtAuthenticationToken auth = jwtAuth("admin", "ROLE_ADMIN");

        service.delete(docId, auth);

        verify(storage).delete("s3-key");
        verify(documents).delete(doc);
        verify(events).publish(eq("DOCUMENT"), eq(docId.toString()), eq("DOCUMENT_DELETED"), any());
    }

    @Test
    void getAuthorizedDocumentThrowsWhenMissing() {
        DocumentRepository documents = Mockito.mock(DocumentRepository.class);
        DocumentMapper mapper = Mockito.mock(DocumentMapper.class);
        S3StorageService storage = Mockito.mock(S3StorageService.class);
        DomainEventPublisher events = Mockito.mock(DomainEventPublisher.class);
        ConsentClient consentClient = Mockito.mock(ConsentClient.class);
        it.sanitech.commons.security.DeptGuard deptGuard = Mockito.mock(it.sanitech.commons.security.DeptGuard.class);

        DocumentService service = new DocumentService(documents, mapper, storage, events, consentClient, deptGuard);

        UUID docId = UUID.randomUUID();
        when(documents.findById(docId)).thenReturn(Optional.empty());

        JwtAuthenticationToken auth = jwtAuth("admin", "ROLE_ADMIN");

        assertThatThrownBy(() -> service.getAuthorizedDocument(docId, auth))
                .isInstanceOf(NotFoundException.class);
    }

    private static JwtAuthenticationToken jwtAuth(String subject, String... authorities) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim(it.sanitech.docs.utilities.AppConstants.Security.CLAIM_PATIENT_ID, 99L)
                .build();
        return new JwtAuthenticationToken(jwt, java.util.Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList());
    }
}
