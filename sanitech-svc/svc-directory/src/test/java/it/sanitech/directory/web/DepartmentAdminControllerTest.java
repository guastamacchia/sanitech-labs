package it.sanitech.directory.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.services.DepartmentService;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
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

@WebMvcTest(DepartmentAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() throws Exception {
        DepartmentCreateDto request = TestDataFactory.departmentCreateDto();
        DepartmentDto response = TestDataFactory.departmentDto(7L, "CARD", "Cardiologia", "HOSP_CENTRAL");

        when(departmentService.create(any(DepartmentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post(AppConstants.ApiPath.ADMIN_DEPARTMENTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CARD"))
                .andExpect(jsonPath("$.name").value("Cardiologia"));
    }

    @Test
    void shouldUpdateDepartment() throws Exception {
        DepartmentUpdateDto request = TestDataFactory.departmentUpdateDto();
        DepartmentDto response = TestDataFactory.departmentDto(7L, "CARD", "Cardiologia interventistica", "HOSP_CENTRAL");

        when(departmentService.update(anyLong(), any(DepartmentUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put(AppConstants.ApiPath.ADMIN_DEPARTMENTS + "/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cardiologia interventistica"));
    }

    @Test
    void shouldSearchDepartments() throws Exception {
        DepartmentDto department = TestDataFactory.departmentDto(5L, "CARD", "Cardiologia", "HOSP_CENTRAL");

        when(departmentService.search("Card")).thenReturn(List.of(department));

        mockMvc.perform(get(AppConstants.ApiPath.ADMIN_DEPARTMENTS)
                        .param("q", "Card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CARD"));
    }

    @Test
    void shouldDeleteDepartment() throws Exception {
        doNothing().when(departmentService).delete(5L);

        mockMvc.perform(delete(AppConstants.ApiPath.ADMIN_DEPARTMENTS + "/{id}", 5L))
                .andExpect(status().isOk());

        verify(departmentService).delete(5L);
    }
}
