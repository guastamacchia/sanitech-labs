package it.sanitech.directory.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.services.SpecializationService;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.services.dto.create.SpecializationCreateDto;
import it.sanitech.directory.services.dto.update.SpecializationUpdateDto;
import it.sanitech.directory.utilities.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpecializationAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpecializationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpecializationService specializationService;

    @Test
    void shouldCreateSpecialization() throws Exception {
        SpecializationCreateDto request = TestDataFactory.specializationCreateDto();
        SpecializationDto response = TestDataFactory.specializationDto(12L, "CARDIO", "Cardiologia clinica");

        when(specializationService.create(any(SpecializationCreateDto.class))).thenReturn(response);

        mockMvc.perform(post(AppConstants.ApiPath.ADMIN_SPECIALIZATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CARDIO"))
                .andExpect(jsonPath("$.name").value("Cardiologia clinica"));
    }

    @Test
    void shouldUpdateSpecialization() throws Exception {
        SpecializationUpdateDto request = TestDataFactory.specializationUpdateDto();
        SpecializationDto response = TestDataFactory.specializationDto(
                12L,
                "CARDIO",
                "Cardiologia interventistica"
        );

        when(specializationService.update(anyLong(), any(SpecializationUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put(AppConstants.ApiPath.ADMIN_SPECIALIZATIONS + "/{id}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cardiologia interventistica"));
    }

    @Test
    void shouldSearchSpecializations() throws Exception {
        SpecializationDto specialization = TestDataFactory.specializationDto(
                15L,
                "DERM",
                "Dermatologia"
        );

        when(specializationService.search("Der")).thenReturn(List.of(specialization));

        mockMvc.perform(get(AppConstants.ApiPath.ADMIN_SPECIALIZATIONS)
                        .param("q", "Der"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("DERM"));
    }

    @Test
    void shouldDeleteSpecialization() throws Exception {
        doNothing().when(specializationService).delete(15L);

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_SPECIALIZATIONS + "/{id}", 15L))
                .andExpect(status().isOk());

        verify(specializationService).delete(15L);
    }
}
