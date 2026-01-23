package it.sanitech.docs.repositories.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Entità JPA che rappresenta i metadati di un documento clinico.
 *
 * <p>
 * Il contenuto binario del file non è salvato su Postgres ma su storage S3/MinIO;
 * qui conserviamo soltanto:
 * <ul>
 *   <li>identificativi e ownership (paziente, uploader);</li>
 *   <li>metadati (nome file, content-type, size, checksum);</li>
 *   <li>chiave oggetto S3 (s3Key).</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "documents",
        indexes = {
                @Index(name = "idx_documents_patient", columnList = "patient_id, created_at"),
                @Index(name = "idx_documents_department", columnList = "department_code, created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_documents_s3_key", columnNames = "s3_key")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Document {

    /** Identificatore UUID del documento (generato lato applicazione/Hibernate). */
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Id del paziente a cui il documento appartiene (bounded context clinico). */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** Soggetto (sub) del JWT dell'utente che ha caricato il documento (audit/ownership). */
    @Column(name = "uploaded_by", nullable = false, length = 128)
    private String uploadedBy;

    /** Reparto che ha prodotto/gestisce il documento (ABAC: DEPT_*). */
    @Column(name = "department_code", nullable = false, length = 80)
    private String departmentCode;

    /** Tipologia logica del documento (es. REPORT, REFERRAL, PRESCRIPTION). */
    @Column(name = "document_type", nullable = false, length = 64)
    private String documentType;

    /** Nome file originale (visualizzazione). */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** MIME type (es. application/pdf). */
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    /** Dimensione file in byte. */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /** Checksum SHA-256 del contenuto (integrità). */
    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    /** Chiave oggetto su S3/MinIO (univoca). */
    @Column(name = "s3_key", nullable = false, length = 512)
    private String s3Key;

    /** Nota/descrizione opzionale. */
    @Column(name = "description", length = 500)
    private String description;

    /** Timestamp di creazione lato DB. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
