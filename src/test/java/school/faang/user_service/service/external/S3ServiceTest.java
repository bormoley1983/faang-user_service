package school.faang.user_service.service.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.exception.S3Exception;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;
    @InjectMocks
    private S3Service service;

    @BeforeEach
    void setBucket() {
        ReflectionTestUtils.setField(service, "bucketName", "avatars");
    }

    @Test
    void uploadToBucket_checksBucketAndUploadsObject() {
        byte[] bytes = {1, 2, 3};

        service.uploadToBucket("picture.png", bytes, "image/png");

        verify(s3Client).headBucket(any(HeadBucketRequest.class));
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("avatars", captor.getValue().bucket());
        assertEquals("picture.png", captor.getValue().key());
        assertEquals("image/png", captor.getValue().contentType());
        assertEquals(3L, captor.getValue().contentLength());
    }

    @Test
    void uploadToBucket_wrapsSdkUploadFailure() {
        doThrow(mock(SdkException.class)).when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(S3Exception.class,
                () -> service.uploadToBucket("picture.png", new byte[]{1}, "image/png"));
    }

    @Test
    void uploadToBucket_createsMissingBucket() {
        software.amazon.awssdk.services.s3.model.S3Exception notFound =
                mock(software.amazon.awssdk.services.s3.model.S3Exception.class);
        when(notFound.statusCode()).thenReturn(404);
        doThrow(notFound).when(s3Client).headBucket(any(HeadBucketRequest.class));

        service.uploadToBucket("picture.png", new byte[]{1}, "image/png");

        verify(s3Client).createBucket(any(CreateBucketRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadToBucket_wrapsBucketCheckFailure() {
        software.amazon.awssdk.services.s3.model.S3Exception forbidden =
                mock(software.amazon.awssdk.services.s3.model.S3Exception.class);
        when(forbidden.statusCode()).thenReturn(403);
        doThrow(forbidden).when(s3Client).headBucket(any(HeadBucketRequest.class));

        assertThrows(S3Exception.class,
                () -> service.uploadToBucket("picture.png", new byte[]{1}, "image/png"));
    }

    @Test
    void getUnexpiredUrl_returnsPresignedUrl() throws Exception {
        URL expected = new URL("https://example.com/avatar.png");
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(expected);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        assertEquals(expected, service.getUnexpiredUrl("avatars", "avatar.png"));
    }

    @Test
    void getUnexpiredUrl_wrapsSdkFailure() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(mock(SdkException.class));

        assertThrows(S3Exception.class,
                () -> service.getUnexpiredUrl("avatars", "avatar.png"));
    }
}
