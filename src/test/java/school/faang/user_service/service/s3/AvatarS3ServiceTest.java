package school.faang.user_service.service.s3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.utils.image.ImageProcessor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AvatarS3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ImageProcessor imageProcessor;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AvatarS3Service avatarS3Service;

    private final String testBucket = "test-bucket";
    private final String avatarFolder = "avatars";
    private final int largeSize = 1024;
    private final int smallSize = 256;
    private final String s3Endpoint = "http://localhost:9001";
    private final String downloadPath = "/api/v1/buckets/test-bucket/objects/download?prefix=";

    @Test
    void uploadAvatarSuccess() throws Exception {
        setField(avatarS3Service, "largeAvatarMaxSize", largeSize);
        setField(avatarS3Service, "smallAvatarMaxSize", smallSize);
        setField(avatarS3Service, "bucketName", testBucket);
        setField(avatarS3Service, "avatarFolderName", avatarFolder);
        setField(avatarS3Service, "s3Endpoint", s3Endpoint);
        setField(avatarS3Service, "downloadPath", downloadPath);

        ImageProcessor.ImageData largeImageData = mockImageData();
        ImageProcessor.ImageData smallImageData = mockImageData();

        when(imageProcessor.resizeImage(any(), eq(largeSize))).thenReturn(largeImageData);
        when(imageProcessor.resizeImage(any(), eq(smallSize))).thenReturn(smallImageData);

        Pair<UserProfilePic, String> result = avatarS3Service.uploadAvatar(multipartFile, "large");

        ArgumentCaptor<PutObjectRequest> putRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(2)).putObject(putRequestCaptor.capture(), any(RequestBody.class));
        List<PutObjectRequest> requests = putRequestCaptor.getAllValues();

        assertEquals(testBucket, requests.get(0).bucket());
        assertEquals(testBucket, requests.get(1).bucket());
        assertTrue(requests.get(0).key().startsWith(avatarFolder + "/"));

        assertNotNull(result.getFirst().getFileId());
        assertNotNull(result.getFirst().getSmallFileId());

        String largeImageKey = requests.get(0).key();
        String expectedUrl = s3Endpoint + downloadPath + URLEncoder.encode(largeImageKey, StandardCharsets.UTF_8);
        assertEquals(expectedUrl, result.getSecond());
    }

    @Test
    void downloadAvatarSuccess() {
        setField(avatarS3Service, "s3Endpoint", s3Endpoint);
        setField(avatarS3Service, "downloadPath", downloadPath);

        String testKey = "avatars/5a595799-c86f-4996-8e27-7258a367e0c4";
        String expectedUrl = s3Endpoint + downloadPath + URLEncoder.encode(testKey, StandardCharsets.UTF_8);
        String actualUrl = avatarS3Service.downloadAvatar(testKey);
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void deleteAvatarSuccess() {
        setField(avatarS3Service, "bucketName", testBucket);
        String testKey = "avatars/test-image.jpg";

        avatarS3Service.deleteAvatar(testKey);

        ArgumentCaptor<DeleteObjectRequest> deleteRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteRequestCaptor.capture());
        assertEquals(testBucket, deleteRequestCaptor.getValue().bucket());
        assertEquals(testKey, deleteRequestCaptor.getValue().key());
    }

    private ImageProcessor.ImageData mockImageData() {
        ImageProcessor.ImageData imageData = mock(ImageProcessor.ImageData.class);
        when(imageData.getInputStream()).thenReturn(mock(InputStream.class));
        when(imageData.getContentLength()).thenReturn(100L);
        when(imageData.getContentType()).thenReturn("image/jpeg");
        return imageData;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = getField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, fieldName);
            } else {
                throw e;
            }
        }
    }
}
