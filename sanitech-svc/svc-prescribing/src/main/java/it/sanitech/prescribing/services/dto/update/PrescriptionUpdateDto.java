package it.sanitech.prescribing.services.dto.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO utilizzato per aggiornare una prescrizione esistente sostituendone le righe.
 *
 * <p>
 * È pensato per operazioni tipo {@code PUT} (replace) dove il client invia la rappresentazione completa
 * delle righe prescrittive.
 * </p>
 */
public record PrescriptionUpdateDto(

        String notes,

        @NotEmpty
        List<@Valid PrescriptionItemUpdateDto> items
) { }
