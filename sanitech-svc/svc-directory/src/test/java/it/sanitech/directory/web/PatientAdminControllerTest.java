package it.sanitech.directory.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.directory.services.PatientService;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.services.dto.PatientDto;
import it.sanitech.directory.services.dto.create.PatientCreateDto;
import it.sanitech.directory.services.dto.update.PatientUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PatientAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @Test
    void shouldCreatePatient() throws Exception {
        PatientCreateDto request = TestDataFactory.patientCreateDto();
        PatientDto response = TestDataFactory.patientDto(
                1L,
                "Mario",
                "Rossi",
                "nome.cognome@email.it",
                "+39 333 123 4567"
        );

        when(patientService.create(any(PatientCreateDto.class), any())).thenReturn(response);

        mockMvc.perform(post(AppConstants.ApiPath.ADMIN_PATIENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("nome.cognome@email.it"));
    }

    @Test
    void shouldPatchPatient() throws Exception {
        PatientUpdateDto request = TestDataFactory.patientUpdateDto();
        PatientDto response = TestDataFactory.patientDto(
                1L,
                "Maria",
                "Rossi",
                "maria.rossi@email.it",
                "+39 333 123 9999"
        );

        when(patientService.patch(anyLong(), any(PatientUpdateDto.class), any())).thenReturn(response);

        mockMvc.perform(patch(AppConstants.ApiPath.ADMIN_PATIENTS + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria.rossi@email.it"));
    }

    @Test
    void shouldSearchPatients() throws Exception {
        PatientDto patient = TestDataFactory.patientDto(
                9L,
                "Luca",
                "Bianchi",
                "luca.bianchi@email.it",
                null
        );

        when(patientService.searchAdmin(any(), any(), any(Integer.class), any(Integer.class), any()))
                .thenReturn(new PageImpl<>(List.of(patient)));

        mockMvc.perform(get(AppConstants.ApiPath.ADMIN_PATIENTS)
                        .param("q", "Bianchi")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9L))
                .andExpect(jsonPath("$.content[0].email").value("luca.bianchi@email.it"));
    }

    @Test
    void shouldDeletePatient() throws Exception {
        doNothing().when(patientService).delete(5L);

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_PATIENTS + "/{id}", 5L))
                .andExpect(status().isOk());

        verify(patientService).delete(5L);
    }
}
