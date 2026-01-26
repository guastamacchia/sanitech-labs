package it.sanitech.admissions.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.admissions.repositories.entities.AdmissionStatus;
import it.sanitech.admissions.repositories.entities.AdmissionType;
import it.sanitech.admissions.services.AdmissionService;
import it.sanitech.admissions.services.dto.AdmissionDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import it.sanitech.commons.exception.GlobalExceptionHandler;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AdmissionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AdmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdmissionService admissionService;

    @Test
    void listUsesSafePagingAndReturnsContent() throws Exception {
        AdmissionDto dto = new AdmissionDto(
                1L,
                10L,
                "WARD",
                AdmissionType.INPATIENT,
                AdmissionStatus.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                null,
                null
        );
        Page<AdmissionDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(admissionService.list(any(Authentication.class), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get("/api/admissions")
                        .param("page", "-1")
                        .param("size", "500")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(admissionService).list(eq(auth), any(), any(), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void admitReturnsCreatedAdmission() throws Exception {
        AdmissionDto dto = new AdmissionDto(
                20L,
                11L,
                "CARDIO",
                AdmissionType.INPATIENT,
                AdmissionStatus.ACTIVE,
                Instant.parse("2024-01-02T00:00:00Z"),
                null,
                "note",
                2L
        );
        when(admissionService.admit(any(), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admissions")
                        .principal(auth)
                        .contentType("application/json")
                        .content("{" +
                                "\"patientId\":11," +
                                "\"departmentCode\":\"cardio\"," +
                                "\"admissionType\":\"INPATIENT\"," +
                                "\"notes\":\"note\"," +
                                "\"attendingDoctorId\":2" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admissions/20"))
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void dischargeReturnsUpdatedAdmission() throws Exception {
        AdmissionDto dto = new AdmissionDto(
                30L,
                99L,
                "ORTHO",
                AdmissionType.OBSERVATION,
                AdmissionStatus.DISCHARGED,
                Instant.parse("2024-01-02T00:00:00Z"),
                Instant.parse("2024-01-03T00:00:00Z"),
                null,
                null
        );
        when(admissionService.discharge(eq(30L), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admissions/30/discharge").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCHARGED"));
    }
}
