package it.sanitech.docs.web;

import it.sanitech.commons.audit.Auditable;
import it.sanitech.docs.repositories.entities.Document;
import it.sanitech.docs.services.DocumentService;
import it.sanitech.docs.services.dto.DocumentDto;
import it.sanitech.docs.storage.S3StorageService;
import it.sanitech.commons.utilities.SortUtils;
import it.sanitech.docs.utilities.AppConstants;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;

import java.util.UUID;

/**
 * REST API per la gestione dei documenti (metadati + download/upload).
 */
@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;
    private final S3StorageService storage;

    /**
     * Ricerca/lista documenti.
     *
     * <p>
     * Rate-limit applicato per proteggere il servizio in scenari di consultazione massiva.
     * </p>
     */
    @GetMapping(AppConstants.ApiPath.DOCS)
    @RateLimiter(name = "docsApi")
    public ResponseEntity<Page<DocumentDto>> list(@RequestParam(required = false) Long patientId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String[] sort,
                                                  Authentication auth) {

        Sort safeSort = SortUtils.safeSort(
                sort,
                AppConstants.SortField.DOCS_ALLOWED,
                AppConstants.SortField.DOCS_DEFAULT
        );

        Pageable pageable = PageRequest.of(page, size, safeSort);
        Page<DocumentDto> result = service.list(auth, patientId, pageable);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .body(result);
    }

    @GetMapping(AppConstants.ApiPath.DOCS + "/{id}")
    public DocumentDto getMetadata(@PathVariable UUID id, Authentication auth) {
        return service.getMetadata(id, auth);
    }

    @GetMapping(AppConstants.ApiPath.DOCS + "/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id, Authentication auth) {

        Document doc = service.getAuthorizedDocument(id, auth);
        var stream = storage.get(doc.getS3Key());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName().replace("\"", "") + "\"")
                .header("X-Document-Id", doc.getId().toString())
                .body(new InputStreamResource(stream));
    }

    @PostMapping(value = AppConstants.ApiPath.DOCS + "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @Auditable(aggregateType = "DOCUMENT", eventType = "DOCUMENT_UPLOADED", aggregateIdSpel = "id")
    public ResponseEntity<DocumentDto> upload(@RequestPart("file") MultipartFile file,
                                              @RequestParam(required = false) Long patientId,
                                              @RequestParam String departmentCode,
                                              @RequestParam String documentType,
                                              @RequestParam(required = false) String description,
                                              Authentication auth) {

        DocumentDto dto = service.upload(file, patientId, departmentCode, documentType, description, auth);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Elimina un documento caricato dal medico/paziente stesso (ownership verificata).
     */
    @DeleteMapping(AppConstants.ApiPath.DOCS + "/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    @Auditable(aggregateType = "DOCUMENT", eventType = "DOCUMENT_DELETED", aggregateIdParam = "id")
    public ResponseEntity<Void> deleteOwn(@PathVariable UUID id, Authentication auth) {
        service.deleteOwn(id, auth);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina qualsiasi documento (admin-only).
     */
    @DeleteMapping(AppConstants.ApiPath.ADMIN_DOCS + "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(aggregateType = "DOCUMENT", eventType = "DOCUMENT_DELETED", aggregateIdParam = "id")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        service.delete(id, auth);
        return ResponseEntity.noContent().build();
    }
}
