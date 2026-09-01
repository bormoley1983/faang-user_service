package school.faang.user_service.publisher.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.events.UserDeactivationEvent;
import school.faang.user_service.exception.EventSerializationException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDeactivationEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UserDeactivationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new UserDeactivationEventPublisher(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "userDeactivationTopic", "user-deactivation-topic");
    }

    @Test
    void publishEvent_ShouldSendSerializedEventWithUserIdKey() throws Exception {
        UserDeactivationEvent event = UserDeactivationEvent.builder()
                .userId(42L)
                .timestamp(Instant.now())
                .build();
        when(kafkaTemplate.send(eq("user-deactivation-topic"), eq("42"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publishEvent(event);

        verify(kafkaTemplate).send(eq("user-deactivation-topic"), eq("42"), anyString());
    }

    @Test
    void publishEvent_ShouldThrowOnSerializationFailure() throws JsonProcessingException {
        UserDeactivationEvent event = UserDeactivationEvent.builder()
                .userId(42L)
                .timestamp(Instant.now())
                .build();
        doThrow(mock(JsonProcessingException.class)).when(objectMapper).writeValueAsString(event);

        assertThrows(EventSerializationException.class, () -> publisher.publishEvent(event));
    }

    @Test
    void publishEvent_ShouldThrowOnKafkaFailure() throws Exception {
        UserDeactivationEvent event = UserDeactivationEvent.builder()
                .userId(42L)
                .timestamp(Instant.now())
                .build();
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("kafka down"));
        when(kafkaTemplate.send(eq("user-deactivation-topic"), eq("42"), anyString())).thenReturn(failed);

        assertThrows(EventSerializationException.class, () -> publisher.publishEvent(event));
    }

}
