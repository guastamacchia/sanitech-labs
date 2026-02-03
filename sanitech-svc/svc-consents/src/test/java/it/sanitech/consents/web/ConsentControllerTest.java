package it.sanitech.consents.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.ConsentStatus;
import it.sanitech.consents.services.ConsentService;
import it.sanitech.consents.services.dto.ConsentCheckResponse;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.utilities.AppConstants;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ConsentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class ConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentService consentService;

    @Test
    void checkReturnsResponse() throws Exception {
        String path = AppConstants.ApiPath.CONSENTS_CHECK;
        ConsentCheckResponse response = new ConsentCheckResponse(
                1L,
                2L,
                ConsentScope.RECORDS,
                true,
                ConsentStatus.GRANTED,
                Instant.parse("2025-01-01T00:00:00Z")
        );
        when(consentService.check(1L, 2L, ConsentScope.RECORDS)).thenReturn(response);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get(path)
                        .param("patientId", "1")
                        .param("doctorId", "2")
                        .param("scope", "RECORDS")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));
    }

    @Test
    void myConsentsUsesPatientClaim() throws Exception {
        String path = AppConstants.ApiPath.CONSENTS_ME + "/doctors";
        ConsentDto dto = new ConsentDto(
                10L,
                77L,
                12L,
                ConsentScope.DOCS,
                ConsentStatus.GRANTED,
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                null,
                Instant.parse("2024-01-02T00:00:00Z")
        );
        when(consentService.listForPatient(77L)).thenReturn(List.of(dto));

        JwtAuthenticationToken auth = patientAuth(77L);

        mockMvc.perform(get(path).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void grantUsesPatientClaim() throws Exception {
        String path = AppConstants.ApiPath.CONSENTS_ME + "/doctors";
        ConsentDto dto = new ConsentDto(
                20L,
                77L,
                12L,
                ConsentScope.PRESCRIPTIONS,
                ConsentStatus.GRANTED,
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2024-01-02T00:00:00Z")
        );
        when(consentService.grantForPatient(eq(77L), any(), any())).thenReturn(dto);

        JwtAuthenticationToken auth = patientAuth(77L);

        mockMvc.perform(post(path)
                        .principal(auth)
                        .contentType("application/json")
                        .content("{" +
                                "\"doctorId\":12," +
                                "\"scope\":\"PRESCRIPTIONS\"," +
                                "\"expiresAt\":\"2025-01-01T00:00:00Z\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void revokeUsesPatientClaim() throws Exception {
        String path = AppConstants.ApiPath.CONSENTS_ME + "/doctors/12/DOCS";

        JwtAuthenticationToken auth = patientAuth(77L);

        mockMvc.perform(delete(path).principal(auth))
                .andExpect(status().isOk());

        verify(consentService).revokeForPatient(eq(77L), eq(12L), eq(ConsentScope.DOCS), any());
    }

    private static JwtAuthenticationToken patientAuth(Long patientId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(AppConstants.Security.CLAIM_PATIENT_ID, patientId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_PATIENT")));
    }
}
