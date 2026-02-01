package it.sanitech.directory.services;

import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.repositories.FacilityRepository;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.services.dto.create.FacilityCreateDto;
import it.sanitech.directory.services.dto.update.FacilityUpdateDto;
import it.sanitech.directory.services.mapper.FacilityMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FacilityMapper facilityMapper;

    @InjectMocks
    private FacilityService facilityService;

    @Test
    void shouldCreateFacility() {
        FacilityCreateDto dto = TestDataFactory.facilityCreateDto();
        Facility saved = Facility.builder()
                .id(2L)
                .code("HOSP_NORD")
                .name("Ospedale Nord")
                .build();
        FacilityDto mappedDto = TestDataFactory.facilityDto(2L, "HOSP_NORD", "Ospedale Nord");

        when(facilityRepository.existsByCodeIgnoreCase("HOSP_NORD")).thenReturn(false);
        when(facilityRepository.save(any(Facility.class))).thenReturn(saved);
        when(facilityMapper.toDto(saved)).thenReturn(mappedDto);

        FacilityDto result = facilityService.create(dto);

        ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
        verify(facilityRepository).save(captor.capture());
        Facility entity = captor.getValue();

        assertThat(entity.getCode()).isEqualTo("HOSP_NORD");
        assertThat(entity.getName()).isEqualTo("Ospedale Nord");
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldUpdateFacility() {
        FacilityUpdateDto dto = TestDataFactory.facilityUpdateDto();
        Facility existing = TestDataFactory.centralHospitalFacility();
        Facility saved = TestDataFactory.centralHospitalFacility();
        saved.setName("Ospedale Centrale Rinnovato");
        FacilityDto mappedDto = TestDataFactory.facilityDto(1L, "HOSP_CENTRAL", "Ospedale Centrale Rinnovato");

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            FacilityUpdateDto update = invocation.getArgument(0);
            Facility entity = invocation.getArgument(1);
            entity.setName(update.name());
            return null;
        }).when(facilityMapper).updateEntity(eq(dto), eq(existing));
        when(facilityRepository.save(any(Facility.class))).thenReturn(saved);
        when(facilityMapper.toDto(saved)).thenReturn(mappedDto);

        FacilityDto result = facilityService.update(1L, dto);

        ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
        verify(facilityRepository).save(captor.capture());
        Facility entity = captor.getValue();

        assertThat(entity.getName()).isEqualTo("Ospedale Centrale Rinnovato");
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldSearchFacilitiesWithBlankQuery() {
        Facility facility = TestDataFactory.centralHospitalFacility();
        FacilityDto mappedDto = TestDataFactory.facilityDto(1L, "HOSP_CENTRAL", "Ospedale Centrale");

        when(facilityRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(facility));
        when(facilityMapper.toDto(facility)).thenReturn(mappedDto);

        List<FacilityDto> result = facilityService.search(" ");

        assertThat(result).containsExactly(mappedDto);
    }

    @Test
    void shouldSearchFacilitiesWithQuery() {
        Facility facility = TestDataFactory.centralHospitalFacility();
        FacilityDto mappedDto = TestDataFactory.facilityDto(1L, "HOSP_CENTRAL", "Ospedale Centrale");

        when(facilityRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc("Central", "Central"))
                .thenReturn(List.of(facility));
        when(facilityMapper.toDto(facility)).thenReturn(mappedDto);

        List<FacilityDto> result = facilityService.search("Central");

        assertThat(result).containsExactly(mappedDto);
    }

    @Test
    void shouldDeleteFacility() {
        Facility facility = TestDataFactory.centralHospitalFacility();

        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));

        facilityService.delete(1L);

        verify(facilityRepository).delete(facility);
    }

    @Test
    void shouldGetByCodeRequired() {
        Facility facility = TestDataFactory.centralHospitalFacility();

        when(facilityRepository.findByCodeIgnoreCase("HOSP_CENTRAL")).thenReturn(Optional.of(facility));

        Facility result = facilityService.getByCodeRequired(" hosp_central ");

        assertThat(result).isEqualTo(facility);
    }
}
