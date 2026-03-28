package school.faang.user_service.service.s3;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.exception.FileException;
import school.faang.user_service.utils.image.ImageProcessor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AvatarS3Service {
    private final S3Client s3Client;
    private final ImageProcessor imageProcessor;

    @Value("${image.avatar.sizes.large}")
    private int largeAvatarMaxSize;

    @Value("${image.avatar.sizes.small}")
    private int smallAvatarMaxSize;

    @Value("${s3.bucketName}")
    private String bucketName;

    @Value("${image.avatar.folder-name}")
    private String avatarFolderName;

    @Value("${s3.endpoint}")
    private String s3Endpoint;

    @Value("${s3.download-path}")
    private String downloadPath;

    public Pair<UserProfilePic, String> uploadAvatar(MultipartFile file, String size) {
        try {
            ImageProcessor.ImageData largeImageData = imageProcessor.resizeImage(file, largeAvatarMaxSize);
            ImageProcessor.ImageData smallImageData = imageProcessor.resizeImage(file, smallAvatarMaxSize);

            String largeImageKey = String.format("%s/%s", avatarFolderName, UUID.randomUUID());
            String smallImageKey = String.format("%s/%s", avatarFolderName, UUID.randomUUID());

            PutObjectRequest largeImagePutObjectRequest = buildPutObjectRequest(largeImageKey, largeImageData);
            PutObjectRequest smallImagePutObjectRequest = buildPutObjectRequest(smallImageKey, smallImageData);

            s3Client.putObject(largeImagePutObjectRequest,
                    RequestBody.fromInputStream(largeImageData.getInputStream(), largeImageData.getContentLength()));
            s3Client.putObject(smallImagePutObjectRequest,
                    RequestBody.fromInputStream(smallImageData.getInputStream(), smallImageData.getContentLength()));

            UserProfilePic userProfilePic = new UserProfilePic(largeImageKey, smallImageKey);

            String avatarUrl = s3Endpoint + downloadPath + URLEncoder.encode(smallImageKey, StandardCharsets.UTF_8);
            if (size.equalsIgnoreCase("large")) {
                avatarUrl = s3Endpoint + downloadPath + URLEncoder.encode(largeImageKey, StandardCharsets.UTF_8);
            }

            return Pair.of(userProfilePic, avatarUrl);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FileException(e.getMessage());
        }
    }

    public String downloadAvatar(String imageKey) {
        return s3Endpoint + downloadPath + URLEncoder.encode(imageKey, StandardCharsets.UTF_8);
    }

    public void deleteAvatar(String imageKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(imageKey)
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FileException(e.getMessage());
        }
    }

    private PutObjectRequest buildPutObjectRequest(String imageKey, ImageProcessor.ImageData imageData) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(imageKey)
                .contentLength(imageData.getContentLength())
                .contentType(imageData.getContentType())
                .build();
    }
}
