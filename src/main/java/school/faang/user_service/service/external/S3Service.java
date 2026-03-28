package school.faang.user_service.service.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.faang.user_service.exception.S3Exception;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private static final Duration PRESIGNED_URL_TTL = Duration.ofDays(7);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name:default-bucket}")
    private String bucketName;

    public void uploadToBucket(String fileName, byte[] data, String contentType) {
        log.info("Uploading file to S3 bucket: {}, fileName: {}", bucketName, fileName);

        ensureBucketExists(bucketName);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.info("File uploaded successfully: {}", fileName);
        } catch (SdkException e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new S3Exception("Error uploading file to S3", e);
        }
    }

    public URL getUnexpiredUrl(String bucketName, String fileId) {
        log.info("Generating presigned URL for file: {}, bucket: {}", fileId, bucketName);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileId)
                    .build();
            GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_TTL)
                    .getObjectRequest(getObjectRequest)
                    .build();
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(request);
            URL url = presignedRequest.url();
            log.info("Presigned URL generated successfully for file: {}", fileId);
            return url;
        } catch (SdkException e) {
            log.error("Failed to generate presigned URL for file: {}, bucket: {}", fileId, bucketName, e);
            throw new S3Exception("Error generating presigned URL", e);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("Bucket does not exist, creating: {}", bucketName);
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
                return;
            }
            log.error("Failed to ensure bucket exists: {}", bucketName, e);
            throw new S3Exception("Error ensuring bucket existence", e);
        } catch (SdkException e) {
            log.error("Failed to ensure bucket exists: {}", bucketName, e);
            throw new S3Exception("Error ensuring bucket existence", e);
        }
    }
}
