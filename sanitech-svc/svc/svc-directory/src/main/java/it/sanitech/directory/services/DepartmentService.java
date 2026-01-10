package it.sanitech.directory.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import it.sanitech.directory.services.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service applicativo per l'anagrafica Reparti.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;

    @Transactional(readOnly = true)
    public List<DepartmentDto> list() {
        return repository.findAll(Sort.by("code").ascending())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> search(String q) {
        if (q == null || q.isBlank()) {
            return list();
        }
        String like = q.trim();
        return repository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(like, like)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public DepartmentDto create(DepartmentCreateDto dto) {
        String code = normalizeCode(dto.code());

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Esiste già un reparto con codice '" + code + "'.");
        }

        Department entity = Department.builder()
                .code(code)
                .name(dto.name().trim())
                .build();

        return mapper.toDto(repository.save(entity));
    }

    public DepartmentDto update(Long id, DepartmentUpdateDto dto) {
        Department entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Reparto", id));
        mapper.updateEntity(dto, entity);
        entity.setName(entity.getName().trim());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Long id) {
        Department entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Reparto", id));
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Department getByCodeRequired(String code) {
        String normalized = normalizeCode(code);
        return repository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Reparto non valido: '" + normalized + "'."));
    }

    private String normalizeCode(String code) {
        if (code == null) throw new IllegalArgumentException("Codice reparto obbligatorio.");
        return code.trim().toUpperCase();
    }
}
