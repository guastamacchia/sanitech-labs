package it.sanitech.docs.storage;

import it.sanitech.docs.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

/**
 * Servizio di storage per salvare e recuperare i file su S3/MinIO.
 */
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3;
    private final S3Properties props;

    public void put(String key, InputStream inputStream, long size, String contentType) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        s3.putObject(req, RequestBody.fromInputStream(inputStream, size));
    }

    public ResponseInputStream<GetObjectResponse> get(String key) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build();
        return s3.getObject(req);
    }

    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build();
        s3.deleteObject(req);
    }
}
