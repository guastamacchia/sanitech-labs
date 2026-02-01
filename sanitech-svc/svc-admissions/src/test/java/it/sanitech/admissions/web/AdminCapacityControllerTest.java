package it.sanitech.admissions.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.admissions.services.CapacityService;
import it.sanitech.admissions.services.dto.CapacityDto;
import it.sanitech.commons.exception.GlobalExceptionHandler;
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
        controllers = AdminCapacityController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AdminCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CapacityService capacityService;

    @Test
    void setCapacityUpdatesDepartment() throws Exception {
        CapacityDto dto = new CapacityDto("CARDIO", 15, 4, 11, Instant.parse("2024-02-10T00:00:00Z"));
        when(capacityService.upsert(eq("CARDIO"), anyInt())).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("user", "pwd", "ROLE_ADMIN");

        mockMvc.perform(put("/api/admin/departments/CARDIO/capacity")
                        .principal(auth)
                        .contentType("application/json")
                        .content("{\"totalBeds\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBeds").value(15))
                .andExpect(jsonPath("$.availableBeds").value(11));
    }
}
