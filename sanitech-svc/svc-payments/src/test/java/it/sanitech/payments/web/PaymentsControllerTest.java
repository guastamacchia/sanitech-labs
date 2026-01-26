package it.sanitech.payments.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = PaymentsController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentOrderService paymentOrderService;

    @Test
    void listReturnsPage() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                10L,
                11L,
                77L,
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
        Page<PaymentOrderDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(paymentOrderService.listForCurrentUser(any(Pageable.class), any(Authentication.class))).thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("patient", "pwd", AppConstants.Security.ROLE_PATIENT);

        mockMvc.perform(get("/api/payments")
                        .param("page", "0")
                        .param("size", "1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(paymentOrderService).listForCurrentUser(pageableCaptor.capture(), eq(auth));
    }

    @Test
    void getReturnsPayment() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                12L,
                11L,
                77L,
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
        when(paymentOrderService.getById(eq(12L), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("patient", "pwd", AppConstants.Security.ROLE_PATIENT);

        mockMvc.perform(get("/api/payments/12").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));
    }

    @Test
    void createUsesIdempotencyHeader() throws Exception {
        PaymentOrderDto dto = new PaymentOrderDto(
                13L,
                11L,
                77L,
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
        when(paymentOrderService.createForCurrentPatient(any(), eq("idem-1"), any(Authentication.class)))
                .thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("patient", "pwd", AppConstants.Security.ROLE_PATIENT);

        mockMvc.perform(post("/api/payments")
                        .principal(auth)
                        .header(AppConstants.Headers.X_IDEMPOTENCY_KEY, "idem-1")
                        .contentType("application/json")
                        .content("{" +
                                "\"appointmentId\":11," +
                                "\"amountCents\":1500," +
                                "\"currency\":\"EUR\"," +
                                "\"method\":\"CARD\"," +
                                "\"description\":\"Visit\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13));
    }
}
