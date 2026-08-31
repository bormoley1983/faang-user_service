package school.faang.user_service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.annotation.PublishProfileViewEvent;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.event.AnalyticsProfileViewEvent;
import school.faang.user_service.publisher.AnalyticsProfileViewPublisher;
import school.faang.user_service.service.user.UserService;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileViewAspectTest {

    @Mock
    private AnalyticsProfileViewPublisher profileViewPublisher;
    @Mock
    private UserContext userContext;
    @Mock
    private UserService userService;
    @Mock
    private JoinPoint joinPoint;
    @Mock
    private Signature signature;

    private ProfileViewAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new ProfileViewAspect(profileViewPublisher, userContext, userService);
    }

    @Test
    void handleEvent_whenResultNull_doesNotPublish() {
        // Arrange
        PublishProfileViewEvent annotation = mockAnnotation();
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("getUser");

        // Act
        aspect.handleEvent(joinPoint, annotation, null);

        // Assert
        verifyNoInteractions(profileViewPublisher);
    }

    @Test
    void handleEvent_whenUserIdMissing_doesNotPublish() {
        // Arrange
        PublishProfileViewEvent annotation = mockAnnotation();
        when(userContext.getUserId()).thenThrow(new IllegalArgumentException("User ID is missing"));

        // Act
        aspect.handleEvent(joinPoint, annotation, "someResult");

        // Assert
        verifyNoInteractions(profileViewPublisher);
    }

    @Test
    void handleEvent_whenSingleResult_publishesEvent() {
        // Arrange
        PublishProfileViewEvent annotation = mockAnnotation();
        when(userContext.getUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(null);

        // Act
        aspect.handleEvent(joinPoint, annotation, "singleResult");

        // Assert
        verify(profileViewPublisher).publishEvent("singleResult");
    }

    @Test
    void handleEvent_whenListResult_publishesForEachElement() {
        // Arrange
        PublishProfileViewEvent annotation = mockAnnotation();
        when(userContext.getUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(null);
        List<String> results = List.of("a", "b", "c");

        // Act
        aspect.handleEvent(joinPoint, annotation, results);

        // Assert
        verify(profileViewPublisher, times(3)).publishEvent(any());
    }

    @SuppressWarnings("unchecked")
    private PublishProfileViewEvent mockAnnotation() {
        PublishProfileViewEvent annotation = mock(PublishProfileViewEvent.class);
        lenient().when(annotation.events()).thenReturn(new Class[]{AnalyticsProfileViewEvent.class});
        return annotation;
    }
}
