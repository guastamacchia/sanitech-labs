package it.sanitech.directory.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.repositories.SpecializationRepository;
import it.sanitech.directory.repositories.entities.Specialization;
import it.sanitech.directory.services.dto.SpecializationDto;
import it.sanitech.directory.services.dto.create.SpecializationCreateDto;
import it.sanitech.directory.services.dto.update.SpecializationUpdateDto;
import it.sanitech.directory.services.mapper.SpecializationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service applicativo per l'anagrafica specializzazioni.
 *
 * <p>
 * Fornisce operazioni di lettura e manutenzione, includendo normalizzazione dei codici
 * e validazioni di unicità per evitare duplicati di dominio.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SpecializationService {

    private final SpecializationRepository repository;
    private final SpecializationMapper mapper;

    @Transactional(readOnly = true)
    public List<SpecializationDto> list() {
        return repository.findAll(Sort.by("code").ascending())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecializationDto> search(String q) {
        if (q == null || q.isBlank()) {
            return list();
        }
        String like = q.trim();
        return repository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(like, like)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public SpecializationDto create(SpecializationCreateDto dto) {
        String code = normalizeCode(dto.code());

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Esiste già una specializzazione con codice '" + code + "'.");
        }

        Specialization entity = Specialization.builder()
                .code(code)
                .name(dto.name().trim())
                .build();

        return mapper.toDto(repository.save(entity));
    }

    public SpecializationDto update(Long id, SpecializationUpdateDto dto) {
        Specialization entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Specializzazione", id));
        mapper.updateEntity(dto, entity);
        entity.setName(entity.getName().trim());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Long id) {
        Specialization entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Specializzazione", id));
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Specialization getByCodeRequired(String code) {
        String normalized = normalizeCode(code);
        return repository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione non valida: '" + normalized + "'."));
    }

    private String normalizeCode(String code) {
        if (code == null) throw new IllegalArgumentException("Codice specializzazione obbligatorio.");
        return code.trim().toUpperCase();
    }
}
