package school.faang.user_service.publisher.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import school.faang.user_service.dto.event.EventStartDto;
import school.faang.user_service.exception.EventSerializationException;
import school.faang.user_service.mapper.event.EventStartMapper;
import school.faang.user_service.model.events.NotificationEventStartEvent;
import school.faang.user_service.publisher.EventPublisher;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationEventStartEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventStartMapper eventStartMapper;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.event-start-topic.name}")
    private String eventStartEventTopic;

    @Override
    public void publishEvent(Object dto) {
        if (!(dto instanceof EventStartDto eventStartDto)) {
            log.error("Invalid DTO type: {}", dto.getClass().getName());
            return;
        }

        NotificationEventStartEvent event = eventStartMapper.toNotificationEventStartEvent(eventStartDto);
        log.info("Publishing event start for eventId: {} with {} participants to Kafka",
                event.getEventId(), event.getUserIds().size());

        String jsonEvents;
        try {
            jsonEvents = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            // Fail loudly so the caller can retry / not commit offsets.
            log.error("Failed to serialize NotificationEventStartEvent for eventId: {}", event.getEventId(), e);
            throw new EventSerializationException(
                    "Unable to serialize notification event-start event for eventId " + event.getEventId(), e);
        }

        try {
            kafkaTemplate.send(eventStartEventTopic, jsonEvents).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventSerializationException(
                    "Interrupted while publishing notification event-start event for eventId " + event.getEventId(), e);
        } catch (ExecutionException e) {
            log.error("Failed to publish NotificationEventStartEvent for eventId: {}", event.getEventId(), e);
            throw new EventSerializationException(
                    "Unable to publish notification event-start event for eventId " + event.getEventId(), e);
        }
    }
}
