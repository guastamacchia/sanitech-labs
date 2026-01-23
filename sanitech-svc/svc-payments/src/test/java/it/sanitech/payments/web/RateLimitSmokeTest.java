package it.sanitech.payments.web;

import it.sanitech.payments.BasePostgresTest;
import it.sanitech.payments.utilities.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test: RateLimiter deve restituire 429 alla seconda chiamata ravvicinata.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitSmokeTest extends BasePostgresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void secondCallShouldReturn429() throws Exception {
        String url = AppConstants.Api.API_BASE + AppConstants.Api.PAYMENTS;

        mockMvc.perform(get(url).with(jwt()
                        .authorities(new SimpleGrantedAuthority(AppConstants.Security.ROLE_PATIENT))
                        .jwt(j -> j.claim(AppConstants.Security.CLAIM_PATIENT_ID, 200L))
                ))
                .andExpect(status().isOk());

        mockMvc.perform(get(url).with(jwt()
                        .authorities(new SimpleGrantedAuthority(AppConstants.Security.ROLE_PATIENT))
                        .jwt(j -> j.claim(AppConstants.Security.CLAIM_PATIENT_ID, 200L))
                ))
                .andExpect(status().isTooManyRequests());
    }
}
