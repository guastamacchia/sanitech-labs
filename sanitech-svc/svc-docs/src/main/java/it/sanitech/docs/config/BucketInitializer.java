package it.sanitech.docs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Inizializzatore bucket S3/MinIO (opzionale).
 *
 * <p>
 * In ambienti locali (docker-compose) è comodo creare automaticamente il bucket.
 * In produzione si può disabilitare con {@code sanitech.docs.s3.auto-create-bucket=false}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BucketInitializer {

    private final S3Client s3;
    private final S3Properties props;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucketExists() {
        if (!props.isAutoCreateBucket()) {
            log.info("Auto-creazione bucket disabilitata (sanitech.docs.s3.auto-create-bucket=false).");
            return;
        }

        String bucket = props.getBucket();
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket S3/MinIO presente: {}", bucket);
        } catch (NoSuchBucketException ex) {
            log.warn("Bucket S3/MinIO non presente, creazione in corso: {}", bucket);
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket creato: {}", bucket);
        } catch (Exception ex) {
            if (props.isFailOnInitError()) {
                // Fail-fast: se lo storage non è disponibile, l'app non può funzionare correttamente.
                throw new IllegalStateException("Impossibile verificare/creare il bucket S3/MinIO: " + bucket, ex);
            }
            log.warn(
                    "Impossibile verificare/creare il bucket S3/MinIO: {}. Avvio senza storage disponibile.",
                    bucket,
                    ex);
        }
    }
}
