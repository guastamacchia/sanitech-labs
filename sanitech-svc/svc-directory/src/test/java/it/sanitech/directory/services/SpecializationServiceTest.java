package it.sanitech.directory.services;

import it.sanitech.directory.TestDataFactory;
import it.sanitech.directory.repositories.SpecializationRepository;
import it.sanitech.directory.repositories.entities.Specialization;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.services.dto.create.SpecializationCreateDto;
import it.sanitech.directory.services.dto.update.SpecializationUpdateDto;
import it.sanitech.directory.services.mapper.SpecializationMapper;
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
class SpecializationServiceTest {

    @Mock
    private SpecializationRepository specializationRepository;

    @Mock
    private SpecializationMapper specializationMapper;

    @InjectMocks
    private SpecializationService specializationService;

    @Test
    void shouldCreateSpecialization() {
        SpecializationCreateDto dto = TestDataFactory.specializationCreateDto();
        Specialization saved = TestDataFactory.cardiologySpecialization();
        SpecializationDto mappedDto = TestDataFactory.specializationDto(20L, "CARDIO", "Cardiologia clinica");

        when(specializationRepository.existsByCodeIgnoreCase("CARDIO")).thenReturn(false);
        when(specializationRepository.save(any(Specialization.class))).thenReturn(saved);
        when(specializationMapper.toDto(saved)).thenReturn(mappedDto);

        SpecializationDto result = specializationService.create(dto);

        ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
        verify(specializationRepository).save(captor.capture());
        Specialization entity = captor.getValue();

        assertThat(entity.getCode()).isEqualTo("CARDIO");
        assertThat(entity.getName()).isEqualTo("Cardiologia clinica");
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldUpdateSpecialization() {
        SpecializationUpdateDto dto = TestDataFactory.specializationUpdateDto();
        Specialization existing = TestDataFactory.cardiologySpecialization();
        Specialization saved = TestDataFactory.cardiologySpecialization();
        saved.setName("Cardiologia interventistica");
        SpecializationDto mappedDto = TestDataFactory.specializationDto(20L, "CARDIO", "Cardiologia interventistica");

        when(specializationRepository.findById(20L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            SpecializationUpdateDto update = invocation.getArgument(0);
            Specialization entity = invocation.getArgument(1);
            entity.setName(update.name());
            return null;
        }).when(specializationMapper).updateEntity(eq(dto), eq(existing));
        when(specializationRepository.save(any(Specialization.class))).thenReturn(saved);
        when(specializationMapper.toDto(saved)).thenReturn(mappedDto);

        SpecializationDto result = specializationService.update(20L, dto);

        ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
        verify(specializationRepository).save(captor.capture());
        Specialization entity = captor.getValue();

        assertThat(entity.getName()).isEqualTo("Cardiologia interventistica");
        assertThat(result).isEqualTo(mappedDto);
    }

    @Test
    void shouldSearchSpecializationsWithBlankQuery() {
        Specialization specialization = TestDataFactory.cardiologySpecialization();
        SpecializationDto mappedDto = TestDataFactory.specializationDto(20L, "CARDIO", "Cardiologia clinica");

        when(specializationRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(specialization));
        when(specializationMapper.toDto(specialization)).thenReturn(mappedDto);

        List<SpecializationDto> result = specializationService.search(" ");

        assertThat(result).containsExactly(mappedDto);
    }

    @Test
    void shouldSearchSpecializationsWithQuery() {
        Specialization specialization = TestDataFactory.cardiologySpecialization();
        SpecializationDto mappedDto = TestDataFactory.specializationDto(20L, "CARDIO", "Cardiologia clinica");

        when(specializationRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc("Card", "Card"))
                .thenReturn(List.of(specialization));
        when(specializationMapper.toDto(specialization)).thenReturn(mappedDto);

        List<SpecializationDto> result = specializationService.search("Card");

        assertThat(result).containsExactly(mappedDto);
    }

    @Test
    void shouldDeleteSpecialization() {
        Specialization specialization = TestDataFactory.cardiologySpecialization();

        when(specializationRepository.findById(20L)).thenReturn(Optional.of(specialization));

        specializationService.delete(20L);

        verify(specializationRepository).delete(specialization);
    }

    @Test
    void shouldGetByCodeRequired() {
        Specialization specialization = TestDataFactory.cardiologySpecialization();

        when(specializationRepository.findByCodeIgnoreCase("CARDIO")).thenReturn(Optional.of(specialization));

        Specialization result = specializationService.getByCodeRequired(" cardio ");

        assertThat(result).isEqualTo(specialization);
    }
}
