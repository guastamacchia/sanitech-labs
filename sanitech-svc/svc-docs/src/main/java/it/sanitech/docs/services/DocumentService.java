package it.sanitech.docs.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.commons.security.DeptGuard;
import it.sanitech.commons.security.SecurityUtils;
import it.sanitech.docs.integrations.consents.ConsentClient;
import it.sanitech.outbox.core.DomainEventPublisher;
import it.sanitech.docs.repositories.DocumentRepository;
import it.sanitech.docs.repositories.entities.Document;
import it.sanitech.docs.services.dto.DocumentDto;
import it.sanitech.docs.services.mappers.DocumentMapper;
import it.sanitech.docs.storage.S3StorageService;
import it.sanitech.docs.utilities.AuthUtils;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service applicativo per gestione documenti (metadati + storage).
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final String AGG_DOCUMENT = "DOCUMENT";
    private static final String EVT_UPLOADED = "DOCUMENT_UPLOADED";
    private static final String EVT_DELETED = "DOCUMENT_DELETED";

    private final DocumentRepository documents;
    private final DocumentMapper mapper;
    private final S3StorageService storage;
    private final DomainEventPublisher events;
    private final ConsentClient consentClient;
    private final DeptGuard deptGuard;

    /**
     * Carica un documento su storage e salva i metadati su DB.
     *
     * <p>
     * Nota di consistenza: lo storage è un sistema esterno rispetto al DB.
     * In caso di errore DB dopo l'upload, eseguiamo una cancellazione compensativa dell'oggetto S3.
     * </p>
     */
    @Transactional
    public DocumentDto upload(MultipartFile file,
                              Long patientId,
                              String departmentCode,
                              String documentType,
                              String description,
                              Authentication auth) {

        JwtAuthenticationToken jwt = AuthUtils.requireJwt(auth);

        if (!SecurityUtils.isAdmin(auth) && !SecurityUtils.isDoctor(auth)) {
            throw new AccessDeniedException("Solo ADMIN o DOCTOR possono caricare documenti.");
        }

        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("patientId non valido.");
        }
        if (departmentCode == null || departmentCode.isBlank()) {
            throw new IllegalArgumentException("departmentCode obbligatorio.");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("documentType obbligatorio.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file obbligatorio.");
        }

        // ABAC: un DOCTOR può caricare documenti solo per i propri reparti.
        if (SecurityUtils.isDoctor(auth)) {
            deptGuard.checkCanManage(departmentCode, auth);
        }

        String normalizedDept = departmentCode.trim().toUpperCase();
        String normalizedType = documentType.trim().toUpperCase();

        String contentType = (file.getContentType() == null || file.getContentType().isBlank())
                ? "application/octet-stream"
                : file.getContentType();

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile leggere il file caricato.", e);
        }

        String sha256 = sha256Hex(bytes);

        String s3Key = "docs/" + patientId + "/" + UUID.randomUUID();

        // 1) Upload su storage
        try {
            storage.put(s3Key, new ByteArrayInputStream(bytes), bytes.length, contentType);
        } catch (Exception e) {
            throw new IllegalStateException("Errore durante il salvataggio del file su storage.", e);
        }

        // 2) Persistenza metadati + outbox (stessa TX)
        try {
            Document saved = documents.save(Document.builder()
                    .patientId(patientId)
                    .uploadedBy(jwt.getName())
                    .departmentCode(normalizedDept)
                    .documentType(normalizedType)
                    .fileName(file.getOriginalFilename() == null ? "documento" : file.getOriginalFilename())
                    .contentType(contentType)
                    .sizeBytes((long) bytes.length)
                    .checksumSha256(sha256)
                    .s3Key(s3Key)
                    .description(description)
                    .build());

            events.publish(AGG_DOCUMENT, saved.getId().toString(), EVT_UPLOADED, Map.of(
                    "documentId", saved.getId(),
                    "patientId", saved.getPatientId(),
                    "departmentCode", saved.getDepartmentCode(),
                    "documentType", saved.getDocumentType()
            ), it.sanitech.docs.utilities.AppConstants.Outbox.TOPIC_AUDITS_EVENTS);

            return mapper.toDto(saved);

        } catch (Exception e) {
            // Compensazione: se il DB fallisce, rimuoviamo l'oggetto già caricato.
            try { storage.delete(s3Key); } catch (Exception ignored) {}
            throw e;
        }
    }

    /**
     * Lista documenti con regole di accesso:
     * <ul>
     *   <li>ADMIN: può listare tutti o filtrare per paziente;</li>
     *   <li>DOCTOR: deve specificare patientId, passa da controllo consenso, e vede solo i documenti dei reparti autorizzati;</li>
     *   <li>PATIENT: vede solo i propri documenti (claim {@code pid}).</li>
     * </ul>
     */
    @Bulkhead(name = "docsRead", type = Bulkhead.Type.SEMAPHORE)
    @Transactional(readOnly = true)
    public Page<DocumentDto> list(Authentication auth, Long patientId, Pageable pageable) {

        if (SecurityUtils.isAdmin(auth)) {
            Page<Document> page = (patientId == null)
                    ? documents.findAll(pageable)
                    : documents.findByPatientId(patientId, pageable);
            return page.map(mapper::toDto);
        }

        if (SecurityUtils.isDoctor(auth)) {
            if (patientId == null) {
                throw new IllegalArgumentException("patientId è obbligatorio per il ruolo DOCTOR.");
            }

            JwtAuthenticationToken jwt = AuthUtils.requireJwt(auth);
            consentClient.assertConsentForDocs(patientId, jwt);

            Set<String> depts = SecurityUtils.departmentCodes(auth);
            if (depts.isEmpty()) {
                throw new AccessDeniedException("Nessun reparto associato all'utenza.");
            }

            return documents.findByPatientIdAndDepartmentCodeIn(patientId, depts, pageable)
                    .map(mapper::toDto);
        }

        if (SecurityUtils.isPatient(auth)) {
            Long pid = AuthUtils.patientId(auth)
                    .orElseThrow(() -> new AccessDeniedException("Claim pid mancante per utenza PATIENT."));
            return documents.findByPatientId(pid, pageable).map(mapper::toDto);
        }

        throw new AccessDeniedException("Non sei autorizzato ad accedere ai documenti.");
    }

    /**
     * Recupera i metadati di un documento verificando le stesse regole di autorizzazione della download.
     */
    @Transactional(readOnly = true)
    public DocumentDto getMetadata(UUID id, Authentication auth) {
        Document doc = getAuthorizedDocument(id, auth);
        return mapper.toDto(doc);
    }

    /**
     * Recupera un documento e ne verifica l'autorizzazione.
     *
     * @return l'entità documento autorizzata
     */
    @Transactional(readOnly = true)
    public Document getAuthorizedDocument(UUID id, Authentication auth) {
        Document doc = documents.findById(id).orElseThrow(() -> NotFoundException.of("Documento", id));

        if (SecurityUtils.isAdmin(auth)) {
            return doc;
        }

        if (SecurityUtils.isDoctor(auth)) {
            JwtAuthenticationToken jwt = AuthUtils.requireJwt(auth);

            // 1) Consenso paziente (svc-consents)
            consentClient.assertConsentForDocs(doc.getPatientId(), jwt);

            // 2) ABAC reparto: il medico accede solo ai documenti dei reparti autorizzati.
            deptGuard.checkCanManage(doc.getDepartmentCode(), auth);

            return doc;
        }

        if (SecurityUtils.isPatient(auth)) {
            Long pid = AuthUtils.patientId(auth)
                    .orElseThrow(() -> new AccessDeniedException("Claim pid mancante per utenza PATIENT."));
            if (!pid.equals(doc.getPatientId())) {
                throw new AccessDeniedException("Non sei autorizzato ad accedere a questo documento.");
            }
            return doc;
        }

        throw new AccessDeniedException("Non sei autorizzato ad accedere al documento.");
    }

    /**
     * Cancella un documento (metadati + storage) e genera evento Outbox.
     *
     * <p>
     * Endpoint tipicamente riservato ad ADMIN.
     * </p>
     */
    @Transactional
    public void delete(UUID id, Authentication auth) {
        if (!SecurityUtils.isAdmin(auth)) {
            throw new AccessDeniedException("Solo ADMIN può eliminare documenti.");
        }

        Document doc = documents.findById(id).orElseThrow(() -> NotFoundException.of("Documento", id));

        storage.delete(doc.getS3Key());
        documents.delete(doc);

        events.publish(AGG_DOCUMENT, id.toString(), EVT_DELETED, Map.of(
                "documentId", id,
                "patientId", doc.getPatientId(),
                "departmentCode", doc.getDepartmentCode(),
                "documentType", doc.getDocumentType()
        ), it.sanitech.docs.utilities.AppConstants.Outbox.TOPIC_AUDITS_EVENTS);
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile calcolare checksum SHA-256.", e);
        }
    }
}
