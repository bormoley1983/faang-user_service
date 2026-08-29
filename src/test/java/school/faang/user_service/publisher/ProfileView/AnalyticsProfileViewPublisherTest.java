package school.faang.user_service.publisher.ProfileView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.entity.User;
import school.faang.user_service.event.AnalyticsProfileViewEvent;
import school.faang.user_service.publisher.AnalyticsProfileViewPublisher;
import school.faang.user_service.service.user.UserService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsProfileViewPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private UserService userService;

    @Mock
    private UserContext userContext;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private AnalyticsProfileViewPublisher analyticsProfileViewPublisher;

    @Captor
    private ArgumentCaptor<String> captor;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                analyticsProfileViewPublisher,
                "analyticsProfileViewTopic",
                "analytics_profile_view_topic");
        user = User.builder()
                .id(123L)
                .build();
    }

    @Test
    void testPublishEvent_Success() throws JsonProcessingException {
        when(userContext.getUserId()).thenReturn(456L);

        AnalyticsProfileViewEvent event = AnalyticsProfileViewEvent.builder()
                .userId(123L)
                .viewerUserId(456L)
                .timestamp(Instant.now())
                .build();

        analyticsProfileViewPublisher.publishEvent(user);

        verify(kafkaTemplate).send(anyString(), captor.capture());

        String capturedJson = captor.getValue();

        AnalyticsProfileViewEvent capturedEvent = objectMapper.readValue(capturedJson, AnalyticsProfileViewEvent.class);

        assertNotNull(capturedEvent);
        assertEquals(event.getUserId(), capturedEvent.getUserId());
        assertEquals(event.getViewerUserId(), capturedEvent.getViewerUserId());
        assertNotNull(capturedEvent.getEventId());
        assertNotNull(capturedEvent.getTimestamp());
    }

    @Test
    void testPublishEvent_UserEqualIds() {
        when(userContext.getUserId()).thenReturn(user.getId());

        analyticsProfileViewPublisher.publishEvent(user);

        verifyNoInteractions(kafkaTemplate);
    }
}
