package it.sanitech.televisit.services.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO per l'aggiornamento delle note cliniche di una televisita.
 */
public record TelevisitNotesDto(

        @Size(max = 10000, message = "Le note non possono superare 10000 caratteri")
        String notes

) {
}
