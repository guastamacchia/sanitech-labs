package it.sanitech.audit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test RateLimiter: verifica che la 2a chiamata venga limitata (HTTP 429).
 */
@Testcontainers
@SpringBootTest(properties = {
        "resilience4j.ratelimiter.instances.auditApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.auditApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.auditApi.timeoutDuration=0",
        "sanitech.audit.ingestion.enabled=false" // evitiamo consumer Kafka nel test
})
@AutoConfigureMockMvc
class RateLimitSmokeTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sanitech_audit_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    MockMvc mvc;

    @Test
    void secondCallShouldReturn429() throws Exception {
        var req = get("/api/audit/events")
                .with(jwt().authorities(() -> "ROLE_ADMIN"));

        mvc.perform(req).andExpect(status().isOk());
        mvc.perform(req).andExpect(status().isTooManyRequests());
    }
}
