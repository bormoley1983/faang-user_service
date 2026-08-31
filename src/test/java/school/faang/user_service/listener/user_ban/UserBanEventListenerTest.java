package school.faang.user_service.listener.user_ban;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.service.user.UserService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBanEventListenerTest {

    @Mock
    private UserService userService;
    @Mock
    private ObjectMapper objectMapper;

    private UserBanEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserBanEventListener(userService, objectMapper);
    }

    @Test
    void listenEvent_whenValidJson_updatesBannedField() throws JsonProcessingException {
        // Arrange
        String json = "{\"userId\":42,\"banned\":true}";
        school.faang.user_service.events.UserBanEvent event =
                school.faang.user_service.events.UserBanEvent.builder().userId(42L).banned(true).build();
        when(objectMapper.readValue(json, school.faang.user_service.events.UserBanEvent.class)).thenReturn(event);

        // Act
        listener.listenEvent(json);

        // Assert
        verify(userService).setBannedField(42L, true);
    }

    @Test
    void listenEvent_whenUnban_updatesBannedFieldToFalse() throws JsonProcessingException {
        // Arrange
        String json = "{\"userId\":7,\"banned\":false}";
        school.faang.user_service.events.UserBanEvent event =
                school.faang.user_service.events.UserBanEvent.builder().userId(7L).banned(false).build();
        when(objectMapper.readValue(json, school.faang.user_service.events.UserBanEvent.class)).thenReturn(event);

        // Act
        listener.listenEvent(json);

        // Assert
        verify(userService).setBannedField(7L, false);
    }

    @Test
    void listenEvent_whenInvalidJson_throwsRuntimeException() throws JsonProcessingException {
        // Arrange
        String invalidJson = "not-json";
        when(objectMapper.readValue(invalidJson, school.faang.user_service.events.UserBanEvent.class))
                .thenThrow(new JsonProcessingException("bad json") {});

        // Act / Assert
        assertThatThrownBy(() -> listener.listenEvent(invalidJson))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
        verifyNoInteractions(userService);
    }
}
