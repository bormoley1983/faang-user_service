package school.faang.user_service.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.avatar.AvatarType;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.UserProfilePic;
import school.faang.user_service.service.external.DiceBearService;
import school.faang.user_service.service.external.S3Service;

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