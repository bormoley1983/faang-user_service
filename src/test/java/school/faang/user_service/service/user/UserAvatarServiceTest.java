package school.faang.user_service.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.dto.avatar.AvatarType;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.service.external.DiceBearService;
import school.faang.user_service.service.external.S3Service;

import java.net.URL;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private DiceBearService diceBearService;

    @InjectMocks
    private UserAvatarService userAvatarService;

    // private final String bucketName = "test-bucket";

    @Test
    void generateAvatarForNewUser_uploadsAvatarAndUpdatesUser() throws Exception {
        ReflectionTestUtils.setField(userAvatarService, "bucketName", "avatars");
        User user = User.builder().id(1L).build();
        byte[] avatar = {1, 2, 3};
        URL url = new URL("https://example.com/avatar.jpeg");
        when(diceBearService.generateAvatar(anyString(), eq(AvatarType.JPEG))).thenReturn(avatar);
        when(s3Service.getUnexpiredUrl(eq("avatars"), anyString())).thenReturn(url);

        userAvatarService.generateAvatarForNewUser(user, AvatarType.JPEG);

        verify(s3Service).uploadToBucket(eq(user.getUserProfilePic().getFileId()), eq(avatar), eq(AvatarType.JPEG.getContentType()));
        assertEquals(url.toString(), user.getAboutMe());
    }

    @Test
    void getUserAvatar_returnsPresignedUrl() throws Exception {
        ReflectionTestUtils.setField(userAvatarService, "bucketName", "avatars");
        UserProfilePic profilePic = new UserProfilePic();
        profilePic.setFileId("avatar.png");
        User user = User.builder().id(1L).userProfilePic(profilePic).build();
        URL expected = new URL("https://example.com/avatar.png");
        when(s3Service.getUnexpiredUrl("avatars", "avatar.png")).thenReturn(expected);

        assertEquals(expected, userAvatarService.getUserAvatar(user));
    }

    @Test
    void generateAvatarForNewUser_ShouldThrowException_WhenAvatarGenerationFails() {
        User user = new User();
        user.setId(1L);
        AvatarType avatarType = AvatarType.JPEG;

        when(diceBearService.generateAvatar(anyString(), eq(avatarType)))
                .thenThrow(new RuntimeException("Avatar generation failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userAvatarService.generateAvatarForNewUser(user, avatarType));
        assertEquals("Avatar generation failed", exception.getMessage());

        verify(diceBearService, times(1)).generateAvatar(anyString(), eq(avatarType));
        verifyNoInteractions(s3Service);
    }

    @Test
    void getUserAvatar_ShouldThrowException_WhenUserHasNoAvatar() {
        User user = new User();
        user.setId(1L);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                userAvatarService.getUserAvatar(user));
        assertEquals("No avatar for user 1", exception.getMessage());

        verifyNoInteractions(s3Service);
    }

    @Test
    void getUserAvatar_ShouldThrowException_WhenFileIdIsBlank() {
        User user = new User();
        user.setId(1L);
        UserProfilePic profilePic = new UserProfilePic();
        profilePic.setFileId("");
        user.setUserProfilePic(profilePic);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                userAvatarService.getUserAvatar(user));
        assertEquals("No avatar for user 1", exception.getMessage());

        verifyNoInteractions(s3Service);
    }
}
