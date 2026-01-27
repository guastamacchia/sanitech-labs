package it.sanitech.prescribing.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
import it.sanitech.prescribing.services.PrescriptionService;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.PrescriptionItemDto;
import it.sanitech.prescribing.utilities.AppConstants;
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
        controllers = PatientPrescriptionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class PatientPrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService service;

    @Test
    void listMineReturnsPage() throws Exception {
        Page<PrescriptionDto> page = new PageImpl<>(List.of(sampleDto(10L)), PageRequest.of(0, 1), 1);
        when(service.listMine(eq(PrescriptionStatus.ISSUED), any(Pageable.class), any(Authentication.class))).thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("patient", "pwd", "ROLE_PATIENT");

        mockMvc.perform(get(AppConstants.ApiPath.PRESCRIPTIONS)
                        .param("status", "ISSUED")
                        .param("page", "0")
                        .param("size", "1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listMine(eq(PrescriptionStatus.ISSUED), pageableCaptor.capture(), eq(auth));
    }

    @Test
    void getMineReturnsDto() throws Exception {
        when(service.getMine(eq(15L), any(Authentication.class))).thenReturn(sampleDto(15L));

        Authentication auth = new TestingAuthenticationToken("patient", "pwd", "ROLE_PATIENT");

        mockMvc.perform(get(AppConstants.ApiPath.PRESCRIPTIONS + "/15").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));
    }

    private static PrescriptionDto sampleDto(Long id) {
        return new PrescriptionDto(
                id,
                77L,
                55L,
                "CARDIO",
                PrescriptionStatus.ISSUED,
                "note",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null,
                List.of(new PrescriptionItemDto(1L, "MED-1", "Aspirin", "1cp", "1/die", 7, "after meal", 1))
        );
    }
}
