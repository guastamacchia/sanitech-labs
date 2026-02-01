package it.sanitech.docs.config;

import it.sanitech.docs.utilities.AppConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprietà di integrazione S3/MinIO per lo storage dei documenti.
 *
 * <p>
 * I file binari vengono salvati su S3/MinIO; su Postgres restano solo i metadati
 * (id, paziente, tipo, key, checksum...).
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = AppConstants.ConfigKeys.S3.PREFIX)
public class S3Properties {

    /** Endpoint HTTP di S3/MinIO (es. {@code http://localhost:9000}). */
    private String endpoint = "http://localhost:9000";

    /** Region (obbligatoria per AWS SDK; per MinIO può essere un valore qualsiasi). */
    private String region = "us-east-1";

    /** Access key. */
    private String accessKey = "minio";

    /** Secret key. */
    private String secretKey = "minio123";

    /** Bucket dove salvare gli oggetti. */
    private String bucket = "sanitech-docs";

    /** Abilita path-style (consigliato per MinIO). */
    private boolean pathStyleAccess = true;

    /** Se true, crea automaticamente il bucket se non esiste (solo per ambienti controllati). */
    private boolean autoCreateBucket = true;

    /**
     * Se true, un errore di inizializzazione del bucket blocca l'avvio dell'applicazione.
     * <p>
     * In ambienti locali è spesso preferibile loggare l'errore e continuare.
     * </p>
     */
    private boolean failOnInitError = true;
}
