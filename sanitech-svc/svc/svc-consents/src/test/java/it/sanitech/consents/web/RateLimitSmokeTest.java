package it.sanitech.consents.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test per verificare che il RateLimiter produca HTTP 429.
 */
@SpringBootTest(properties = {
        "resilience4j.ratelimiter.instances.consentsApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.consentsApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.consentsApi.timeoutDuration=0"
})
@AutoConfigureMockMvc
class RateLimitSmokeTest {

    @Autowired
    MockMvc mvc;

    @Test
    void secondCallShouldReturn429() throws Exception {
        var req = get("/api/consents/check")
                .param("patientId", "1")
                .param("doctorId", "1")
                .param("scope", "RECORDS")
                .accept(MediaType.APPLICATION_JSON)
                .with(jwt().authorities(() -> "ROLE_DOCTOR"));

        mvc.perform(req).andExpect(status().isOk());
        mvc.perform(req).andExpect(status().isTooManyRequests());
    }
}
