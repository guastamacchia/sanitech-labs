package it.sanitech.scheduling.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.scheduling.repositories.entities.SlotStatus;
import it.sanitech.scheduling.repositories.entities.VisitMode;
import it.sanitech.scheduling.services.SlotService;
import it.sanitech.scheduling.services.dto.SlotDto;
import java.time.Instant;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = SlotController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class SlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlotService slotService;

    @Test
    void searchReturnsPage() throws Exception {
        Page<SlotDto> page = new PageImpl<>(List.of(sampleDto(10L)), PageRequest.of(0, 1), 1);
        when(slotService.searchAvailableSlots(eq(22L), eq("CARDIO"), eq(VisitMode.IN_PERSON), any(), any(), eq(0), eq(1), any(String[].class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/slots")
                        .param("doctorId", "22")
                        .param("department", "CARDIO")
                        .param("mode", "IN_PERSON")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "startAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));
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
