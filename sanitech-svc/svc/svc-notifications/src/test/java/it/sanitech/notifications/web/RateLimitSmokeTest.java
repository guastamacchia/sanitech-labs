package it.sanitech.notifications.web;

import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test: verifica che il RateLimiter restituisca 429 alla seconda chiamata ravvicinata.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "resilience4j.ratelimiter.instances.notificationsApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.notificationsApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.notificationsApi.timeoutDuration=0"
})
class RateLimitSmokeTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NotificationService service;

    @Test
    void second_call_is_throttled() throws Exception {
        when(service.listForRecipient(any(RecipientType.class), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isTooManyRequests());
    }
}
