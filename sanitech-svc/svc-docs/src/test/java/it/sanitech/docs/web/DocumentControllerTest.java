package it.sanitech.docs.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.docs.exception.DocsExceptionHandler;
import it.sanitech.docs.repositories.entities.Document;
import it.sanitech.docs.services.DocumentService;
import it.sanitech.docs.services.dto.DocumentDto;
import it.sanitech.docs.storage.S3StorageService;
import it.sanitech.docs.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@WebMvcTest(
        controllers = DocumentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {GlobalExceptionHandler.class, DocsExceptionHandler.class})
)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private S3StorageService storageService;

    @Test
    void listReturnsPageAndHeader() throws Exception {
        DocumentDto dto = new DocumentDto(
                UUID.randomUUID(),
                10L,
                "CARDIO",
                "REPORT",
                "report.pdf",
                "application/pdf",
                100L,
                "hash",
                null,
                Instant.parse("2024-01-01T00:00:00Z")
        );
        Page<DocumentDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(documentService.list(any(Authentication.class), any(), any(Pageable.class))).thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get(AppConstants.ApiPath.DOCS)
                        .param("page", "0")
                        .param("size", "1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].patientId").value(10));
    }

    @Test
    void getMetadataReturnsDto() throws Exception {
        UUID docId = UUID.randomUUID();
        DocumentDto dto = new DocumentDto(
                docId,
                10L,
                "CARDIO",
                "REPORT",
                "report.pdf",
                "application/pdf",
                100L,
                "hash",
                null,
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(documentService.getMetadata(eq(docId), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get(AppConstants.ApiPath.DOCS + "/" + docId).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId.toString()));
    }

    @Test
    void downloadReturnsStreamAndHeaders() throws Exception {
        UUID docId = UUID.randomUUID();
        Document doc = Document.builder()
                .id(docId)
                .patientId(10L)
                .departmentCode("CARDIO")
                .documentType("REPORT")
                .fileName("report.pdf")
                .contentType("application/pdf")
                .sizeBytes(100L)
                .checksumSha256("hash")
                .s3Key("s3-key")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
        when(documentService.getAuthorizedDocument(eq(docId), any(Authentication.class))).thenReturn(doc);
        ResponseInputStream<GetObjectResponse> stream = org.mockito.Mockito.mock(ResponseInputStream.class);
        when(storageService.get("s3-key")).thenReturn(stream);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get(AppConstants.ApiPath.DOCS + "/" + docId + "/download").principal(auth))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Document-Id", docId.toString()))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void uploadReturnsCreated() throws Exception {
        UUID docId = UUID.randomUUID();
        DocumentDto dto = new DocumentDto(
                docId,
                10L,
                "CARDIO",
                "REPORT",
                "report.pdf",
                "application/pdf",
                100L,
                "hash",
                "note",
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(documentService.upload(any(MultipartFile.class), eq(10L), eq("CARDIO"), eq("REPORT"), eq("note"), any(Authentication.class)))
                .thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(multipart(AppConstants.ApiPath.DOCS + "/upload")
                        .file("file", "payload".getBytes())
                        .param("patientId", "10")
                        .param("departmentCode", "CARDIO")
                        .param("documentType", "REPORT")
                        .param("description", "note")
                        .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(docId.toString()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        UUID docId = UUID.randomUUID();
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_DOCS + "/" + docId).principal(auth))
                .andExpect(status().isNoContent());

        verify(documentService).delete(eq(docId), any(Authentication.class));
    }
}
