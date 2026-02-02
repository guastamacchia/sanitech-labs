package it.sanitech.directory.services;

import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.DoctorRepository;
import it.sanitech.directory.repositories.FacilityRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import it.sanitech.directory.services.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() {
        DepartmentCreateDto dto = TestDataFactory.departmentCreateDto();
        Facility facility = TestDataFactory.centralHospitalFacility();
        Department saved = TestDataFactory.cardiologyDepartment();
        DepartmentDto mappedDto = TestDataFactory.departmentDto(10L, "CARD", "Cardiologia", "HOSP_CENTRAL");

        when(departmentRepository.existsByCodeIgnoreCase("CARD")).thenReturn(false);
        when(facilityRepository.findByCodeIgnoreCase("HOSP_CENTRAL")).thenReturn(Optional.of(facility));
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);
        when(departmentMapper.toDto(saved)).thenReturn(mappedDto);

        DepartmentDto result = departmentService.create(dto);

        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(captor.capture());
        Department entity = captor.getValue();

        assertThat(entity.getCode()).isEqualTo("CARD");
        assertThat(entity.getName()).isEqualTo("Cardiologia");
        assertThat(entity.getFacility()).isEqualTo(facility);
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldUpdateDepartment() {
        DepartmentUpdateDto dto = TestDataFactory.departmentUpdateDto();
        Department existing = TestDataFactory.cardiologyDepartment();
        Department saved = TestDataFactory.cardiologyDepartment();
        saved.setName("Cardiologia interventistica");
        DepartmentDto mappedDto = TestDataFactory.departmentDto(10L, "CARD", "Cardiologia interventistica", "HOSP_CENTRAL");

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            DepartmentUpdateDto update = invocation.getArgument(0);
            Department entity = invocation.getArgument(1);
            entity.setName(update.name());
            return null;
        }).when(departmentMapper).updateEntity(eq(dto), eq(existing));
        when(departmentRepository.save(any(Department.class))).thenReturn(saved);
        when(departmentMapper.toDto(saved)).thenReturn(mappedDto);

        DepartmentDto result = departmentService.update(10L, dto);

        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(captor.capture());
        Department entity = captor.getValue();

        assertThat(entity.getName()).isEqualTo("Cardiologia interventistica");
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldSearchDepartmentsWithBlankQuery() {
        Department department = TestDataFactory.cardiologyDepartment();
        DepartmentDto mappedDto = TestDataFactory.departmentDto(10L, "CARD", "Cardiologia", "HOSP_CENTRAL");

        when(departmentRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(department));
        when(departmentMapper.toDto(department)).thenReturn(mappedDto);
        when(doctorRepository.countByDepartmentIds(anyList())).thenReturn(List.<Object[]>of(new Object[]{10L, 5L}));

        List<DepartmentDto> result = departmentService.search(" ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("CARD");
        assertThat(result.get(0).doctorCount()).isEqualTo(5L);
    }

    @Test
    void shouldSearchDepartmentsWithQuery() {
        Department department = TestDataFactory.cardiologyDepartment();
        DepartmentDto mappedDto = TestDataFactory.departmentDto(10L, "CARD", "Cardiologia", "HOSP_CENTRAL");

        when(departmentRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc("Card", "Card"))
                .thenReturn(List.of(department));
        when(departmentMapper.toDto(department)).thenReturn(mappedDto);
        when(doctorRepository.countByDepartmentIds(anyList())).thenReturn(List.<Object[]>of(new Object[]{10L, 3L}));

        List<DepartmentDto> result = departmentService.search("Card");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("CARD");
        assertThat(result.get(0).doctorCount()).isEqualTo(3L);
    }

    @Test
    void shouldDeleteDepartment() {
        Department department = TestDataFactory.cardiologyDepartment();

        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));

        departmentService.delete(10L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void shouldGetByCodeRequired() {
        Department department = TestDataFactory.cardiologyDepartment();

        when(departmentRepository.findByCodeIgnoreCase("CARD")).thenReturn(Optional.of(department));

        Department result = departmentService.getByCodeRequired(" card ");

        assertThat(result).isEqualTo(department);
    }
}
