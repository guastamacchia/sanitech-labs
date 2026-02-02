package it.sanitech.directory.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.services.DoctorService;
import it.sanitech.directory.services.dto.DoctorDto;
import it.sanitech.directory.services.dto.create.DoctorCreateDto;
import it.sanitech.directory.services.dto.update.DoctorUpdateDto;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest(DoctorAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    @Test
    void shouldCreateDoctor() throws Exception {
        DoctorCreateDto request = TestDataFactory.doctorCreateDto();
        DoctorDto response = TestDataFactory.doctorDto(
                11L,
                "Luca",
                "Bianchi",
                "luca.bianchi@email.it"
        );

        when(doctorService.create(any(DoctorCreateDto.class), any())).thenReturn(response);

        mockMvc.perform(post(AppConstants.ApiPath.ADMIN_DOCTORS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.email").value("luca.bianchi@email.it"));
    }

    @Test
    void shouldPatchDoctor() throws Exception {
        DoctorUpdateDto request = TestDataFactory.doctorUpdateDto();
        DoctorDto response = TestDataFactory.doctorDto(
                11L,
                "Luca",
                "Bianchi",
                "nuova.email@email.it"
        );

        when(doctorService.patch(anyLong(), any(DoctorUpdateDto.class), any())).thenReturn(response);

        mockMvc.perform(patch(AppConstants.ApiPath.ADMIN_DOCTORS + "/{id}", 11L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Luca"))
                .andExpect(jsonPath("$.email").value("nuova.email@email.it"));
    }

    @Test
    void shouldSearchDoctors() throws Exception {
        DoctorDto doctor = TestDataFactory.doctorDto(
                22L,
                "Sara",
                "Neri",
                "sara.neri@email.it"
        );

        when(doctorService.search(anyString(), anyString(), anyString(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(doctor)));

        mockMvc.perform(get(AppConstants.ApiPath.ADMIN_DOCTORS)
                        .param("q", "Neri")
                        .param("department", "CARD")
                        .param("facility", "HOSP_CENTRAL")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(22L))
                .andExpect(jsonPath("$.content[0].email").value("sara.neri@email.it"));
    }

    @Test
    void shouldDeleteDoctor() throws Exception {
        doNothing().when(doctorService).delete(13L);

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_DOCTORS + "/{id}", 13L))
                .andExpect(status().isOk());

        verify(doctorService).delete(13L);
    }
}
