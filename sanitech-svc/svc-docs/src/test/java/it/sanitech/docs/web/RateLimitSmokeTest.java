package it.sanitech.docs.web;

import it.sanitech.docs.services.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test per verificare che il RateLimiter (Resilience4j) sia attivo sulle API read.
 */
@WebMvcTest(controllers = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration({AopAutoConfiguration.class,
        io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration.class})
@TestPropertySource(properties = {
        "resilience4j.ratelimiter.instances.docsApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.docsApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.docsApi.timeoutDuration=0"
})
class RateLimitSmokeTest {

    @MockBean
    DocumentService service;

    @org.springframework.beans.factory.annotation.Autowired
    MockMvc mvc;

    @BeforeEach
    void setup() {
        Mockito.when(service.list(nullable(org.springframework.security.core.Authentication.class), any(), any()))
                .thenReturn(Page.empty());
    }

    @Test
    void secondCall_shouldReturn429() throws Exception {
        mvc.perform(get("/api/docs")).andExpect(status().isOk());
        mvc.perform(get("/api/docs")).andExpect(status().isTooManyRequests());
    }
}
