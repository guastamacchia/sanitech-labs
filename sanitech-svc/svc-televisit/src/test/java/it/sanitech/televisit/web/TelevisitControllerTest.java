package it.sanitech.televisit.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.televisit.repositories.entities.TelevisitStatus;
import it.sanitech.televisit.services.TelevisitService;
import it.sanitech.televisit.services.dto.LiveKitTokenDto;
import it.sanitech.televisit.services.dto.TelevisitDto;
import it.sanitech.televisit.utilities.AppConstants;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

@WebMvcTest(
        controllers = TelevisitController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class TelevisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TelevisitService service;

    @Test
    void searchReturnsPage() throws Exception {
        Page<TelevisitDto> page = new PageImpl<>(List.of(sampleDto(10L)), PageRequest.of(0, 1), 1);
        when(service.search(eq("CARDIO"), eq(TelevisitStatus.CREATED), any(), any(), any(Pageable.class), any())).thenReturn(page);

        mockMvc.perform(get(AppConstants.ApiPath.TELEVISITS)
                        .param("department", "CARDIO")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(eq("CARDIO"), eq(TelevisitStatus.CREATED), eq(null), eq(null), pageableCaptor.capture(), any());
    }

    @Test
    void getReturnsDto() throws Exception {
        when(service.getById(12L)).thenReturn(sampleDto(12L));

        mockMvc.perform(get(AppConstants.ApiPath.TELEVISITS + "/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));
    }

    @Test
    void doctorTokenReturnsToken() throws Exception {
        LiveKitTokenDto tokenDto = new LiveKitTokenDto("tv-room", "https://livekit", "token", 3600);
        when(service.issueDoctorToken(eq(15L), any(Authentication.class))).thenReturn(tokenDto);

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.TELEVISITS + "/15/token/doctor").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomName").value("tv-room"));
    }

    @Test
    void startReturnsDto() throws Exception {
        when(service.start(eq(20L), any(Authentication.class))).thenReturn(sampleDto(20L));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.TELEVISITS + "/20/start").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void endReturnsDto() throws Exception {
        when(service.end(eq(21L), any(Authentication.class))).thenReturn(sampleDto(21L));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.TELEVISITS + "/21/end").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(21));
    }

    @Test
    void cancelReturnsDto() throws Exception {
        when(service.cancel(eq(22L), any(Authentication.class))).thenReturn(sampleDto(22L));

        Authentication auth = new TestingAuthenticationToken("doc-subject", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.TELEVISITS + "/22/cancel").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22));
    }

    private static TelevisitDto sampleDto(Long id) {
        return new TelevisitDto(
                id,
                "tv-room",
                "CARDIO",
                "doc-subject",
                "patient-subject",
                OffsetDateTime.parse("2024-01-01T10:00:00Z"),
                TelevisitStatus.CREATED,
                null,
                null,
                null
        );
    }
}
