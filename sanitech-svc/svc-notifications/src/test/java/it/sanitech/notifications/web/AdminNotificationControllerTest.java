package it.sanitech.notifications.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        controllers = AdminNotificationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void createReturnsNotification() throws Exception {
        NotificationDto dto = new NotificationDto(
                20L,
                RecipientType.ADMIN,
                "admin",
                NotificationChannel.IN_APP,
                null,
                "Subject",
                "Body",
                NotificationStatus.SENT,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        when(notificationService.create(any())).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/notifications")
                        .principal(auth)
                        .contentType("application/json")
                        .content("{" +
                                "\"recipientType\":\"ADMIN\"," +
                                "\"recipientId\":\"admin\"," +
                                "\"channel\":\"IN_APP\"," +
                                "\"subject\":\"Subject\"," +
                                "\"body\":\"Body\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void bulkCreateReturnsList() throws Exception {
        NotificationDto dto = new NotificationDto(
                21L,
                RecipientType.ADMIN,
                "admin",
                NotificationChannel.IN_APP,
                null,
                "Subject",
                "Body",
                NotificationStatus.SENT,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        when(notificationService.bulkCreate(any())).thenReturn(List.of(dto));

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(post("/api/admin/notifications/_bulk")
                        .principal(auth)
                        .contentType("application/json")
                        .content("[{" +
                                "\"recipientType\":\"ADMIN\"," +
                                "\"recipientId\":\"admin\"," +
                                "\"channel\":\"IN_APP\"," +
                                "\"subject\":\"Subject\"," +
                                "\"body\":\"Body\"" +
                                "}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(21));
    }

    @Test
    void getReturnsNotification() throws Exception {
        NotificationDto dto = new NotificationDto(
                22L,
                RecipientType.ADMIN,
                "admin",
                NotificationChannel.IN_APP,
                null,
                "Subject",
                "Body",
                NotificationStatus.SENT,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
        when(notificationService.get(22L)).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(get("/api/admin/notifications/22").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22));
    }

    @Test
    void deleteRemovesNotification() throws Exception {
        Authentication auth = new TestingAuthenticationToken("admin", "pwd", "ROLE_ADMIN");

        mockMvc.perform(delete("/api/admin/notifications/23").principal(auth))
                .andExpect(status().isOk());

        verify(notificationService).delete(eq(23L));
    }
}
