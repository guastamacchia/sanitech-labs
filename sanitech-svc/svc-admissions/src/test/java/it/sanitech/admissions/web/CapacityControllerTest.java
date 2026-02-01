package it.sanitech.admissions.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.admissions.services.CapacityService;
import it.sanitech.admissions.services.dto.CapacityDto;
import it.sanitech.commons.exception.GlobalExceptionHandler;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = CapacityController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class CapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CapacityService capacityService;

    @Test
    void listReturnsCapacityDtos() throws Exception {
        List<CapacityDto> capacities = List.of(
                new CapacityDto("CARDIO", 10, 2, 8, Instant.parse("2024-01-01T00:00:00Z"))
        );
        when(capacityService.listAll()).thenReturn(capacities);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get("/api/departments/capacity").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentCode").value("CARDIO"))
                .andExpect(jsonPath("$[0].availableBeds").value(8));
    }

    @Test
    void getReturnsCapacityDto() throws Exception {
        CapacityDto capacity = new CapacityDto("NEURO", 12, 5, 7, Instant.parse("2024-02-01T00:00:00Z"));
        when(capacityService.get("NEURO")).thenReturn(capacity);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get("/api/departments/capacity/NEURO").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBeds").value(12))
                .andExpect(jsonPath("$.occupiedBeds").value(5));
    }
}
