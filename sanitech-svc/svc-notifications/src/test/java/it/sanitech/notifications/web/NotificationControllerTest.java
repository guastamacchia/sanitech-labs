package it.sanitech.notifications.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.notifications.repositories.entities.NotificationChannel;
import it.sanitech.notifications.repositories.entities.NotificationStatus;
import it.sanitech.notifications.repositories.entities.RecipientType;
import it.sanitech.notifications.services.NotificationService;
import it.sanitech.notifications.services.dto.NotificationDto;
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
        controllers = NotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void listMyNotificationsReturnsPage() throws Exception {
        NotificationDto dto = new NotificationDto(
                10L,
                RecipientType.PATIENT,
                "p1",
                NotificationChannel.IN_APP,
                null,
                "Subject",
                "Body",
                NotificationStatus.SENT,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        Page<NotificationDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(notificationService.listForRecipient(any(), any(), any(Pageable.class))).thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("p1", "pwd", "ROLE_PATIENT");

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationService).listForRecipient(eq(RecipientType.PATIENT), eq("p1"), pageableCaptor.capture());
    }
}
