package school.faang.user_service.service.external;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.exception.S3Exception;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarS3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    private final String bucketName = "test-bucket";

    @Test
    void getUnexpiredUrl_ShouldReturnPresignedUrl() throws Exception {
        String fileId = "test-file.jpeg";
        URL expectedUrl = URI.create("http://localhost/test-file.jpeg").toURL();
        PresignedGetObjectRequest presignedRequest = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);
        when(presignedRequest.url()).thenReturn(expectedUrl);

        URL actualUrl = s3Service.getUnexpiredUrl(bucketName, fileId);

        assertEquals(expectedUrl.toString(), actualUrl.toString());
        verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void getUnexpiredUrl_ShouldThrowS3Exception_WhenGenerationFails() {
        String fileId = "test-file.jpeg";
        doThrow(SdkException.class).when(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));

        S3Exception exception = assertThrows(S3Exception.class, () -> s3Service.getUnexpiredUrl(bucketName, fileId));
        assertEquals("Error generating presigned URL", exception.getMessage());
    }
}
