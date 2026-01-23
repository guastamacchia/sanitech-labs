package it.sanitech.prescribing.web;

import it.sanitech.prescribing.TestJwtDecoderConfig;
import it.sanitech.prescribing.services.PrescriptionService;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test del RateLimiter su GET /api/prescriptions.
 *
 * <p>
 * Il test disabilita i filtri di sicurezza e mocka il service per concentrarsi sul comportamento
 * del RateLimiter (429 alla seconda chiamata nel periodo).
 * </p>
 */
@Import(TestJwtDecoderConfig.class)
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "resilience4j.ratelimiter.instances.prescribingApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.prescribingApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.prescribingApi.timeoutDuration=0"
})
@AutoConfigureMockMvc(addFilters = false)
class RateLimitSmokeTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PrescriptionService service;

    @BeforeEach
    void setup() {
        when(service.listMine(any(), any(), any()))
                .thenReturn(Page.empty());
    }

    @Test
    void shouldReturn429OnSecondCallWithinWindow() throws Exception {
        mvc.perform(get("/api/prescriptions"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/prescriptions"))
                .andExpect(status().isTooManyRequests());
    }
}
