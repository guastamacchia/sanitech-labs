package it.sanitech.scheduling.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.scheduling.repositories.entities.AppointmentStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.AppointmentService;
import it.sanitech.scheduling.services.dto.AppointmentDto;
import it.sanitech.scheduling.services.dto.create.AppointmentCreateDto;
import java.time.Instant;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AppointmentController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void bookReturnsDto() throws Exception {
        AppointmentDto dto = sampleDto(10L);
        when(appointmentService.book(any(AppointmentCreateDto.class), any(Authentication.class))).thenReturn(dto);

        AppointmentCreateDto request = new AppointmentCreateDto(11L, 77L, null);
        Authentication auth = new TestingAuthenticationToken("patient", "pwd", "ROLE_PATIENT");

        mockMvc.perform(post("/api/appointments")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void searchReturnsPage() throws Exception {
        Page<AppointmentDto> page = new PageImpl<>(List.of(sampleDto(12L)), PageRequest.of(0, 1), 1);
        when(appointmentService.search(eq(77L), eq(22L), eq("CARDIO"), eq(0), eq(1), any(String[].class), any(Authentication.class)))
                .thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get("/api/appointments")
                        .param("patientId", "77")
                        .param("doctorId", "22")
                        .param("department", "CARDIO")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "startAt,desc")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(12));

        ArgumentCaptor<String[]> sortCaptor = ArgumentCaptor.forClass(String[].class);
        verify(appointmentService).search(eq(77L), eq(22L), eq("CARDIO"), eq(0), eq(1), sortCaptor.capture(), eq(auth));
    }

    @Test
    void cancelCallsService() throws Exception {
        Authentication auth = new TestingAuthenticationToken("patient", "pwd", "ROLE_PATIENT");

        mockMvc.perform(delete("/api/appointments/30").principal(auth))
                .andExpect(status().isOk());

        verify(appointmentService).cancel(eq(30L), eq(auth));
    }

    private static AppointmentDto sampleDto(Long id) {
        return new AppointmentDto(
                id,
                11L,
                77L,
                22L,
                "CARDIO",
                VisitMode.IN_PERSON,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:30:00Z"),
                AppointmentStatus.BOOKED,
                null
        );
    }
}
