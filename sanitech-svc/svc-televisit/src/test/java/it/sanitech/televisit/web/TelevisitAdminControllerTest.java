package it.sanitech.televisit.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.services.TelevisitService;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.services.dto.create.TelevisitCreateDto;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = TelevisitAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class TelevisitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelevisitService service;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createReturnsDto() throws Exception {
        TelevisitDto dto = sampleDto(10L);
        when(service.create(any(TelevisitCreateDto.class))).thenReturn(dto);

        TelevisitCreateDto request = new TelevisitCreateDto(
                "doc-subject",
                "patient-subject",
                "CARDIO",
                OffsetDateTime.parse("2024-01-01T10:00:00Z")
        );
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/televisits")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void patientTokenReturnsToken() throws Exception {
        LiveKitTokenDto tokenDto = new LiveKitTokenDto("tv-room", "https://livekit", "token", 3600);
        when(service.issuePatientToken(eq(12L))).thenReturn(tokenDto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/televisits/12/token/patient").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomName").value("tv-room"));
    }

    private static TelevisitDto sampleDto(Long id) {
        return new TelevisitDto(
                id,
                "tv-room",
                "CARDIO",
                "doc-subject",
                "patient-subject",
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                TelevisitStatus.CREATED
        );
    }
}
