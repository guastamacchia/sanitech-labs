package it.sanitech.payments.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.payments.properties.PaymentWebhookProperties;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = PaymentsWebhookController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentsWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentOrderService paymentOrderService;

    @MockBean
    private PaymentWebhookProperties webhookProperties;

    @Test
    void providerUpdateRequiresSecret() throws Exception {
        when(webhookProperties.getSecret()).thenReturn("secret");

        mockMvc.perform(post("/api/webhooks/payments/provider")
                        .contentType("application/json")
                        .content("{" +
                                "\"provider\":\"stripe\"," +
                                "\"providerReference\":\"ref\"," +
                                "\"status\":\"CAPTURED\"" +
                                "}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void providerUpdateAcceptsSecret() throws Exception {
        when(webhookProperties.getSecret()).thenReturn("secret");
        PaymentOrderDto dto = new PaymentOrderDto(
                30L,
                11L,
                77L,
                1500L,
                "EUR",
                PaymentMethod.CARD,
                "STRIPE",
                "ref",
                PaymentStatus.CAPTURED,
                "Visit",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
        when(paymentOrderService.updateFromWebhook(eq("stripe"), eq("ref"), eq(PaymentStatus.CAPTURED))).thenReturn(dto);

        mockMvc.perform(post("/api/webhooks/payments/provider")
                        .header(AppConstants.Headers.X_WEBHOOK_SECRET, "secret")
                        .contentType("application/json")
                        .content("{" +
                                "\"provider\":\"stripe\"," +
                                "\"providerReference\":\"ref\"," +
                                "\"status\":\"CAPTURED\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }
}
