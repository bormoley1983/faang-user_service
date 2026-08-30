package school.faang.user_service.publisher.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.events.UserDeactivationEvent;
import school.faang.user_service.exception.EventSerializationException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDeactivationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.user-deactivation-topic.name}")
    private String userDeactivationTopic;

    public void publishEvent(UserDeactivationEvent event) {
        long userId = event.getUserId();
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(userDeactivationTopic, String.valueOf(userId), json).get();
            log.info("Published user deactivation event for userId={}", userId);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize user deactivation event for userId={}", userId, e);
            throw new EventSerializationException(
                    "Unable to serialize user deactivation event for userId=" + userId, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventSerializationException(
                    "Interrupted while sending user deactivation event for userId=" + userId, e);
        } catch (Exception e) {
            log.error("Failed to send user deactivation event for userId={}", userId, e);
            throw new EventSerializationException(
                    "Failed to send user deactivation event for userId=" + userId, e);
        }
    }
}
