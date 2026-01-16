package it.sanitech.docs.services.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO di lettura per esporre i metadati di un documento.
 *
 * <p>
 * Nota: il contenuto binario del file non è incluso; per lo streaming usare l'endpoint download.
 * </p>
 */
public record DocumentDto(

        /** Identificatore UUID del documento. */
        UUID id,

        /** Id paziente proprietario del documento. */
        Long patientId,

        /** Reparto del documento (ABAC). */
        String departmentCode,

        /** Tipologia documento (es. REPORT). */
        String documentType,

        /** Nome file originale. */
        String fileName,

        /** MIME type. */
        String contentType,

        /** Dimensione in byte. */
        Long sizeBytes,

        /** Checksum SHA-256. */
        String checksumSha256,

        /** Note/descrizione (opzionale). */
        String description,

        /** Data creazione. */
        Instant createdAt

) { }
