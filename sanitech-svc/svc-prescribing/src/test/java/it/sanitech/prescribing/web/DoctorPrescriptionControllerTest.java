package it.sanitech.prescribing.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.commons.exception.GlobalExceptionHandler;
import it.sanitech.prescribing.repositories.entities.PrescriptionStatus;
import it.sanitech.prescribing.services.PrescriptionService;
import it.sanitech.prescribing.services.dto.PrescriptionDto;
import it.sanitech.prescribing.services.dto.PrescriptionItemDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionCreateDto;
import it.sanitech.prescribing.services.dto.create.PrescriptionItemCreateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionPatchDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionItemUpdateDto;
import it.sanitech.prescribing.services.dto.update.PrescriptionUpdateDto;
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
import org.springframework.http.MediaType;

@WebMvcTest(
        controllers = DoctorPrescriptionController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
)
@AutoConfigureMockMvc(addFilters = false)
class DoctorPrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createReturnsDto() throws Exception {
        PrescriptionDto dto = sampleDto(10L);
        when(service.create(any(PrescriptionCreateDto.class), any(Authentication.class))).thenReturn(dto);

        PrescriptionCreateDto request = new PrescriptionCreateDto(
                77L,
                "CARDIO",
                "note",
                List.of(new PrescriptionItemCreateDto("MED-1", "Aspirin", "1cp", "1/die", 7, "after meal", 1))
        );
        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void listForPatientReturnsPage() throws Exception {
        Page<PrescriptionDto> page = new PageImpl<>(List.of(sampleDto(12L)), PageRequest.of(0, 1), 1);
        when(service.listForDoctor(eq(77L), eq("CARDIO"), any(Pageable.class), any(Authentication.class))).thenReturn(page);

        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(get(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS)
                        .param("patientId", "77")
                        .param("departmentCode", "CARDIO")
                        .param("page", "0")
                        .param("size", "1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(12));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listForDoctor(eq(77L), eq("CARDIO"), pageableCaptor.capture(), eq(auth));
    }

    @Test
    void getReturnsDto() throws Exception {
        when(service.getForDoctor(eq(15L), any(Authentication.class))).thenReturn(sampleDto(15L));

        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(get(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS + "/15").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15));
    }

    @Test
    void updateUsesPutEndpoint() throws Exception {
        PrescriptionDto dto = sampleDto(20L);
        when(service.update(eq(20L), any(PrescriptionUpdateDto.class), any(Authentication.class))).thenReturn(dto);

        PrescriptionUpdateDto request = new PrescriptionUpdateDto(
                "new notes",
                List.of(new PrescriptionItemUpdateDto("MED-1", "Aspirin", "2cp", "2/die", 10, "notes", 1))
        );
        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(put(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS + "/20")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void patchUsesPatchEndpoint() throws Exception {
        PrescriptionDto dto = sampleDto(25L);
        when(service.patch(eq(25L), any(PrescriptionPatchDto.class), any(Authentication.class))).thenReturn(dto);

        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(patch(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS + "/25")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrescriptionPatchDto("patch"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(25));
    }

    @Test
    void cancelUsesPostEndpoint() throws Exception {
        Authentication auth = new TestingAuthenticationToken("doctor", "pwd", "ROLE_DOCTOR");

        mockMvc.perform(post(AppConstants.ApiPath.DOCTOR_PRESCRIPTIONS + "/30/cancel").principal(auth))
                .andExpect(status().isOk());

        verify(service).cancel(eq(30L), eq(auth));
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
