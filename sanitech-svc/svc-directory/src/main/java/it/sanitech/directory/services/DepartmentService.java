package it.sanitech.directory.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.repositories.DepartmentRepository;
import it.sanitech.directory.repositories.FacilityRepository;
import it.sanitech.directory.repositories.entities.Department;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.services.dto.DepartmentDto;
import it.sanitech.directory.services.dto.create.DepartmentCreateDto;
import it.sanitech.directory.services.dto.update.DepartmentUpdateDto;
import it.sanitech.directory.services.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service applicativo per l'anagrafica reparti.
 *
 * <p>
 * Gestisce la consultazione e la manutenzione dei reparti, applicando normalizzazioni
 * sui codici e controlli di unicità prima della persistenza.
 * Ogni reparto è associato a una struttura (Facility) del network.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;
    private final FacilityRepository facilityRepository;
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
        if (Objects.isNull(q) || q.isBlank()) {
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

        String facilityCode = normalizeCode(dto.facilityCode());
        Facility facility = resolveFacility(facilityCode);

        Department entity = Department.builder()
                .code(code)
                .name(dto.name().trim())
                .facility(facility)
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

    private Facility resolveFacility(String facilityCode) {
        return facilityRepository.findByCodeIgnoreCase(facilityCode)
                .orElseThrow(() -> new IllegalArgumentException("Struttura non valida: " + facilityCode));
    }

    private String normalizeCode(String code) {
        if (Objects.isNull(code)) throw new IllegalArgumentException("Codice obbligatorio.");
        return code.trim().toUpperCase();
    }
}
