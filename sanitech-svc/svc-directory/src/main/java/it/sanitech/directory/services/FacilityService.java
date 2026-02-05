package it.sanitech.directory.services;

import it.sanitech.commons.exception.NotFoundException;
import it.sanitech.directory.repositories.FacilityRepository;
import it.sanitech.directory.repositories.entities.Facility;
import it.sanitech.directory.services.dto.FacilityDto;
import it.sanitech.directory.services.dto.create.FacilityCreateDto;
import it.sanitech.directory.services.dto.update.FacilityUpdateDto;
import it.sanitech.directory.services.mapper.FacilityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Service applicativo per l'anagrafica strutture (Facility).
 *
 * <p>
 * Fornisce operazioni di lettura e manutenzione delle strutture sanitarie del network,
 * includendo normalizzazione dei codici e validazioni di unicità per evitare duplicati.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacilityService {

    private final FacilityRepository repository;
    private final FacilityMapper mapper;

    @Transactional(readOnly = true)
    public List<FacilityDto> list() {
        return repository.findAll(Sort.by("code").ascending())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacilityDto> search(String q) {
        if (Objects.isNull(q) || q.isBlank()) {
            return list();
        }
        String like = q.trim();
        return repository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrderByCodeAsc(like, like)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public FacilityDto create(FacilityCreateDto dto) {
        String code = normalizeCode(dto.code());

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Esiste già una struttura con codice '" + code + "'.");
        }

        Facility entity = Facility.builder()
                .code(code)
                .name(dto.name().trim())
                .address(dto.address())
                .phone(dto.phone())
                .build();

        return mapper.toDto(repository.save(entity));
    }

    public FacilityDto update(Long id, FacilityUpdateDto dto) {
        Facility entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Struttura", id));
        mapper.updateEntity(dto, entity);
        entity.setName(entity.getName().trim());
        return mapper.toDto(repository.save(entity));
    }

    public void delete(Long id) {
        Facility entity = repository.findById(id).orElseThrow(() -> NotFoundException.of("Struttura", id));
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public Facility getByCodeRequired(String code) {
        String normalized = normalizeCode(code);
        return repository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Struttura non valida: '" + normalized + "'."));
    }

    private String normalizeCode(String code) {
        if (Objects.isNull(code)) throw new IllegalArgumentException("Codice struttura obbligatorio.");
        return code.trim().toUpperCase();
    }
}
