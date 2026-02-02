package it.sanitech.payments.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.payments.repositories.entities.PaymentMethod;
import it.sanitech.payments.repositories.entities.PaymentStatus;
import it.sanitech.payments.services.PaymentOrderService;
import it.sanitech.payments.services.dto.PaymentOrderDto;
import it.sanitech.payments.utilities.AppConstants;
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
        controllers = PaymentsAdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentsAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentOrderService paymentOrderService;

    @Test
    void createReturnsOrder() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                20L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                null,
                PaymentStatus.CREATED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.adminCreate(any(), eq("idem-2"), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(post("/api/admin/payments")
                        .principal(auth)
                        .header(AppConstants.Headers.X_IDEMPOTENCY_KEY, "idem-2")
                        .contentType("application/json")
                        .content("{" +
                                "\"appointmentId\":11," +
                                "\"patientId\":77," +
                                "\"amountCents\":1500," +
                                "\"currency\":\"EUR\"," +
                                "\"method\":\"CARD\"," +
                                "\"description\":\"Visit\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void patchUpdatesOrder() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                21L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                "ref",
                PaymentStatus.CAPTURED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.adminPatch(eq(21L), any())).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(patch("/api/admin/payments/21")
                        .principal(auth)
                        .contentType("application/json")
                        .content("{" +
                                "\"status\":\"CAPTURED\"," +
                                "\"providerReference\":\"ref\"," +
                                "\"description\":\"Visit\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    void captureTriggersService() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                22L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                null,
                PaymentStatus.CAPTURED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.capture(22L)).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(post("/api/admin/payments/22/capture").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    void failTriggersService() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                23L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                "ref",
                PaymentStatus.FAILED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.fail(eq(23L), eq("ref"))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(post("/api/admin/payments/23/fail")
                        .principal(auth)
                        .param("providerReference", "ref"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void cancelTriggersService() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                24L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                null,
                PaymentStatus.CANCELLED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.cancel(24L)).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(post("/api/admin/payments/24/cancel").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void refundTriggersService() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                25L,
                11L,
                77L,
                "patient@test.com",
                "Test Patient",
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "MANUAL",
                null,
                PaymentStatus.REFUNDED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.refund(25L)).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", AppConstants.Security.ROLE_ADMIN);

        mockMvc.perform(post("/api/admin/payments/25/refund").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
