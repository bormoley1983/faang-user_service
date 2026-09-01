package school.faang.user_service.publisher.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.dto.event.EventStartDto;
import school.faang.user_service.exception.EventSerializationException;
import school.faang.user_service.mapper.event.EventStartMapper;
import school.faang.user_service.model.events.NotificationEventStartEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventStartEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private EventStartMapper eventStartMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private NotificationEventStartEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new NotificationEventStartEventPublisher(kafkaTemplate, eventStartMapper, objectMapper);
        ReflectionTestUtils.setField(publisher, "eventStartEventTopic", "event-start-topic");
    }

    @Test
    void publishEvent_ShouldIgnoreNonMatchingDtoType() {
        publisher.publishEvent("not an EventStartDto");

        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void publishEvent_ShouldSendSerializedEventToKafka() throws Exception {
        EventStartDto dto = EventStartDto.builder().eventId(1L).build();
        NotificationEventStartEvent event = new NotificationEventStartEvent(1L, 2L, List.of(3L), null, "msg");
        when(eventStartMapper.toNotificationEventStartEvent(dto)).thenReturn(event);
        when(kafkaTemplate.send(eq("event-start-topic"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishEvent(dto);

        verify(kafkaTemplate).send(eq("event-start-topic"), anyString());
    }

    @Test
    void publishEvent_ShouldThrowOnSerializationFailure() throws JsonProcessingException {
        EventStartDto dto = EventStartDto.builder().eventId(1L).build();
        NotificationEventStartEvent event = new NotificationEventStartEvent(1L, 2L, List.of(3L), null, "msg");
        when(eventStartMapper.toNotificationEventStartEvent(dto)).thenReturn(event);
        doThrow(mock(JsonProcessingException.class)).when(objectMapper).writeValueAsString(event);

        assertThrows(EventSerializationException.class, () -> publisher.publishEvent(dto));
    }

    @Test
    void publishEvent_ShouldThrowOnKafkaExecutionFailure() throws Exception {
        EventStartDto dto = EventStartDto.builder().eventId(1L).build();
        NotificationEventStartEvent event = new NotificationEventStartEvent(1L, 2L, List.of(3L), null, "msg");
        when(eventStartMapper.toNotificationEventStartEvent(dto)).thenReturn(event);
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("kafka down"));
        when(kafkaTemplate.send(eq("event-start-topic"), anyString())).thenReturn(failed);

        assertThrows(EventSerializationException.class, () -> publisher.publishEvent(dto));
    }
}
