package it.sanitech.admissions.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test: RateLimiter su GET /api/admissions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false",
        "resilience4j.ratelimiter.instances.admissionsApi.limitForPeriod=1",
        "resilience4j.ratelimiter.instances.admissionsApi.limitRefreshPeriod=10s",
        "resilience4j.ratelimiter.instances.admissionsApi.timeoutDuration=0"
})
class RateLimitSmokeTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @TestConfiguration
    static class JwtDecoderStubConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            // Evita chiamate remote all'issuer OIDC durante i test.
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test")
                    .claim("scope", "openid")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }

    private final MockMvc mockMvc;

    RateLimitSmokeTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void rateLimit_secondCallReturns429() throws Exception {
        // Prima chiamata OK
        mockMvc.perform(get("/api/admissions")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        // Seconda chiamata nello stesso periodo -> 429
        mockMvc.perform(get("/api/admissions")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isTooManyRequests());
    }
}
