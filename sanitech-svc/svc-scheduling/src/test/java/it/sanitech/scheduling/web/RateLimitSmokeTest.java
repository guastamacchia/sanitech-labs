package it.sanitech.scheduling.web;

import it.sanitech.scheduling.TestJwtDecoderConfig;
import it.sanitech.scheduling.services.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test del RateLimiter su GET /api/slots.
 *
 * <p>
 * Il test disabilita i filtri di sicurezza e mocka il service per concentrarsi sul comportamento
 * del RateLimiter (429 alla seconda chiamata nel periodo).
 * </p>
 */
@Import(TestJwtDecoderConfig.class)
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "resilience4j.ratelimiter.instances.schedulingApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.schedulingApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.schedulingApi.timeoutDuration=0"
})
@AutoConfigureMockMvc(addFilters = false)
class RateLimitSmokeTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SlotService slotService;

    @BeforeEach
    void setup() {
        when(slotService.searchAvailableSlots(any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(Page.empty());
    }

    @Test
    void second_call_returns_429() throws Exception {
        mvc.perform(get("/api/slots"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/slots"))
                .andExpect(status().isTooManyRequests());
    }
}
