package it.sanitech.prescribing.services.dto.update;

/**
 * DTO per aggiornamento parziale (PATCH) della prescrizione.
 *
 * <p>I campi {@code null} vengono ignorati dal livello di service.</p>
 */
public record PrescriptionPatchDto(
        String notes
) { }
