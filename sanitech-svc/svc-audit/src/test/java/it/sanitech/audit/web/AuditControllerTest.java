package it.sanitech.audit.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.audit.services.AuditService;
import it.sanitech.audit.services.dto.AuditEventDto;
import it.sanitech.audit.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import it.sanitech.commons.exception.GlobalExceptionHandler;
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

@WebMvcTest(
        controllers = AuditController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void recordPassesClientIpAndReturnsDto() throws Exception {
        String path = AppConstants.ApiPath.API_BASE + AppConstants.ApiPath.AUDIT_EVENTS;
        AuditEventDto dto = new AuditEventDto(
                10L,
                Instant.parse("2024-01-01T00:00:00Z"),
                AppConstants.Audit.SOURCE_API,
                "USER",
                "alice",
                "LOGIN",
                null,
                null,
                AppConstants.Audit.OUTCOME_SUCCESS,
                "203.0.113.10",
                null,
                null
        );
        when(auditService.recordFromApi(any(), any(Authentication.class), any())).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("alice", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post(path)
                        .principal(auth)
                        .header("X-Forwarded-For", "203.0.113.10")
                        .contentType("application/json")
                        .content("{" +
                                "\"action\":\"LOGIN\"," +
                                "\"resourceType\":\"USER\"," +
                                "\"resourceId\":\"42\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).recordFromApi(any(), eq(auth), ipCaptor.capture());
        assertThat(ipCaptor.getValue()).isEqualTo("203.0.113.10");
    }

    @Test
    void searchReturnsPagedResults() throws Exception {
        String path = AppConstants.ApiPath.API_BASE + AppConstants.ApiPath.AUDIT_EVENTS;
        AuditEventDto dto = new AuditEventDto(
                11L,
                Instant.parse("2024-01-02T00:00:00Z"),
                AppConstants.Audit.SOURCE_API,
                "SERVICE",
                "svc-directory",
                "SYNC",
                null,
                null,
                AppConstants.Audit.OUTCOME_SUCCESS,
                null,
                null,
                null
        );
        Page<AuditEventDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(auditService.search(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get(path)
                        .param("actorId", "svc-directory")
                        .param("action", "SYNC")
                        .param("outcome", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(11));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditService).search(eq("svc-directory"), eq("SYNC"), any(), any(), eq("SUCCESS"), any(), any(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void getReturnsEvent() throws Exception {
        String path = AppConstants.ApiPath.API_BASE + AppConstants.ApiPath.AUDIT_EVENTS + "/12";
        AuditEventDto dto = new AuditEventDto(
                12L,
                Instant.parse("2024-01-03T00:00:00Z"),
                AppConstants.Audit.SOURCE_API,
                "USER",
                "bob",
                "UPDATE",
                null,
                null,
                AppConstants.Audit.OUTCOME_SUCCESS,
                null,
                null,
                null
        );
        when(auditService.getById(12L)).thenReturn(dto);

        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("UPDATE"));
    }
}
