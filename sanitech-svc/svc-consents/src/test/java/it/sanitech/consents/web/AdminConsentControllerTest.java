package it.sanitech.consents.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.consents.repositories.entities.ConsentScope;
import it.sanitech.consents.repositories.entities.ConsentStatus;
import it.sanitech.consents.services.ConsentService;
import it.sanitech.consents.services.dto.ConsentDto;
import it.sanitech.consents.utilities.AppConstants;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AdminConsentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AdminConsentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsentService consentService;

    @Test
    void getReturnsConsent() throws Exception {
        ConsentDto dto = new ConsentDto(
                5L,
                7L,
                9L,
                ConsentScope.RECORDS,
                ConsentStatus.GRANTED,
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                null,
                Instant.parse("2024-01-02T00:00:00Z")
        );
        when(consentService.getById(5L)).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get(AppConstants.ApiPath.ADMIN_CONSENTS + "/5")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void deleteRemovesConsent() throws Exception {
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_CONSENTS + "/5")
                        .with(authentication(auth)))
                .andExpect(status().isOk());

        verify(consentService).deleteById(eq(5L), any());
    }
}
