package it.sanitech.scheduling.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.SlotService;
import it.sanitech.scheduling.services.dto.SlotDto;
import it.sanitech.scheduling.services.dto.create.SlotCreateDto;
import java.time.Instant;
import java.util.List;
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
        controllers = SlotAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class SlotAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlotService slotService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createReturnsDto() throws Exception {
        SlotDto dto = sampleDto(10L);
        when(slotService.createSlot(any(SlotCreateDto.class), any(Authentication.class))).thenReturn(dto);

        SlotCreateDto request = new SlotCreateDto(
                22L,
                "CARDIO",
                VisitMode.IN_PERSON,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:30:00Z")
        );
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/slots")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void bulkReturnsList() throws Exception {
        SlotDto dto = sampleDto(20L);
        when(slotService.createSlotsBulk(any(), any(Authentication.class))).thenReturn(List.of(dto));

        List<SlotCreateDto> request = List.of(new SlotCreateDto(
                22L,
                "CARDIO",
                VisitMode.IN_PERSON,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:30:00Z")
        ));
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/slots/_bulk")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));

        verify(slotService).createSlotsBulk(eq(request), eq(auth));
    }

    @Test
    void cancelCallsService() throws Exception {
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(delete("/api/admin/slots/30").principal(auth))
                .andExpect(status().isOk());

        verify(slotService).cancelSlot(eq(30L), eq(auth));
    }

    private static SlotDto sampleDto(Long id) {
        return new SlotDto(
                id,
                22L,
                "CARDIO",
                VisitMode.IN_PERSON,
                Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:30:00Z"),
                SlotStatus.AVAILABLE
        );
    }
}
