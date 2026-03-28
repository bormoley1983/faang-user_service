package school.faang.user_service.service.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import school.faang.user_service.config.dicebear.DiceBearApiConfig;
import school.faang.user_service.config.dicebear.DicebearStyleGenerator;
import school.faang.user_service.dto.avatar.AvatarType;
import school.faang.user_service.exception.DiceBearException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DiceBearServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DiceBearApiConfig diceBearApiConfig;

    @Mock
    private DicebearStyleGenerator styleGenerator;

    @InjectMocks
    private DiceBearService diceBearService;

    private String filename;
    private String apiUrl;
    private String style;
    private AvatarType avatarType;

    @BeforeEach
    void setUp() {
        filename = "test-avatar";
        apiUrl = "https://api.dicebear.com";
        style = "bottts";
        avatarType = AvatarType.PNG;

        when(diceBearApiConfig.getApiUrl()).thenReturn(apiUrl);
        when(styleGenerator.getRandomStyleString()).thenReturn(style);
    }

    @Test
    void testGenerateAvatar_Success() {
        byte[] expectedAvatar = "avatar-data".getBytes();
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment(style)
                .pathSegment(avatarType.name().toLowerCase())
                .queryParam("seed", filename)
                .queryParam("width", avatarType.getWidth())
                .queryParam("height", avatarType.getHeight())
                .toUriString();

        when(restTemplate.getForObject(url, byte[].class)).thenReturn(expectedAvatar);

        byte[] actualAvatar = diceBearService.generateAvatar(filename, avatarType);

        assertArrayEquals(expectedAvatar, actualAvatar);
        verify(restTemplate, times(1)).getForObject(url, byte[].class);
    }

    @Test
    void testGenerateAvatar_DiceBearException() {
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment(style)
                .pathSegment(avatarType.name().toLowerCase())
                .queryParam("seed", filename)
                .queryParam("width", avatarType.getWidth())
                .queryParam("height", avatarType.getHeight())
                .toUriString();

        when(restTemplate.getForObject(url, byte[].class)).thenThrow(new RuntimeException("API error"));

        DiceBearException exception = assertThrows(DiceBearException.class,
                () -> diceBearService.generateAvatar(filename, avatarType));

        verify(restTemplate, times(3)).getForObject(url, byte[].class);
        assertThrows(DiceBearException.class, () -> {
            throw exception;
        });
    }

    @Test
    void testGenerateAvatar_RetryOnTemporaryFailure() {
        byte[] expectedAvatar = "avatar-data".getBytes();
        String url = UriComponentsBuilder.fromUriString(apiUrl)
                .pathSegment(style)
                .pathSegment(avatarType.name().toLowerCase())
                .queryParam("seed", filename)
                .queryParam("width", avatarType.getWidth())
                .queryParam("height", avatarType.getHeight())
                .toUriString();

        when(restTemplate.getForObject(url, byte[].class))
                .thenThrow(new DiceBearException("Temporary failure"))
                .thenThrow(new DiceBearException("Temporary failure"))
                .thenReturn(expectedAvatar);

        byte[] actualAvatar = diceBearService.generateAvatar(filename, avatarType);

        assertArrayEquals(expectedAvatar, actualAvatar);
        verify(restTemplate, times(3)).getForObject(url, byte[].class);
    }
}
