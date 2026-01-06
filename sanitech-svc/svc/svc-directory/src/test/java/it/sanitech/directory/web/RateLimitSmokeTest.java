package it.sanitech.directory.web;

import it.sanitech.directory.TestJwtDecoderConfig;
import it.sanitech.directory.services.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
    TestJwtDecoderConfig.class,
    RateLimitSmokeTest.MockConfig.class
})
@SpringBootTest(properties = {
    "resilience4j.ratelimiter.instances.directoryApi.limitForPeriod=1",
    "resilience4j.ratelimiter.instances.directoryApi.limitRefreshPeriod=10s",
    "resilience4j.ratelimiter.instances.directoryApi.timeoutDuration=0"
})
@AutoConfigureMockMvc(addFilters = false)
class RateLimitSmokeTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private DoctorService doctorService;

    @BeforeEach
    void setup() {
        when(doctorService.search(any(), any(), any(), anyInt(), anyInt(), any()))
            .thenReturn(Page.empty());
    }

    @Test
    void second_call_returns_429() throws Exception {
        mvc.perform(get("/api/doctors"))
            .andExpect(status().isOk());

        mvc.perform(get("/api/doctors"))
            .andExpect(status().isTooManyRequests());
    }

    @TestConfiguration
    @EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
    static class MockConfig {

        @Bean
        DoctorService doctorService() {
            return Mockito.mock(DoctorService.class);
        }
    }
}
