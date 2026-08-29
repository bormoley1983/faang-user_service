package school.faang.user_service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.entity.User;
import school.faang.user_service.event.AnalyticsProfileViewEvent;
import school.faang.user_service.exception.EventSerializationException;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AnalyticsProfileViewPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserContext userContext;

    @Value("${spring.kafka.topics.analytics-user-profile-view-topic.name}")
    private String analyticsProfileViewTopic;

    @Override
    public void publishEvent(Object user) {
        Long userId = ((User) user).getId();
        Long viewerUserId = userContext.getUserId();

        if (!userId.equals(viewerUserId)) {
            try {
                AnalyticsProfileViewEvent analyticsProfileViewEvent = AnalyticsProfileViewEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .userId(userId)
                        .viewerUserId(viewerUserId)
                        .timestamp(Instant.now())
                        .build();
                String json = objectMapper.writeValueAsString(analyticsProfileViewEvent);
                kafkaTemplate.send(analyticsProfileViewTopic, json);
            } catch (JsonProcessingException e) {
                log.error("Unable to serialize analytics profile-view event for receiverId={}", userId, e);
                throw new EventSerializationException("Unable to serialize analytics profile-view event");
            }
        }
    }
}
