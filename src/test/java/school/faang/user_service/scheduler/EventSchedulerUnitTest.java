package school.faang.user_service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import school.faang.user_service.config.scheduler.EventStartEventNotificationConfig;
import school.faang.user_service.dto.event.EventStartDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.publisher.event.NotificationEventStartEventPublisher;
import school.faang.user_service.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventSchedulerUnitTest {

    @Mock
    private EventService eventService;
    @Mock
    private NotificationEventStartEventPublisher eventStartEventPublisher;
    @Mock
    private EventStartEventNotificationConfig eventNotificationConfig;
    @Mock
    private Environment environment;
    @InjectMocks
    private EventScheduler eventScheduler;

    @Test
    public void testScheduleEventNotifications() {
        EventStartEventNotificationConfig.Interval interval = new EventStartEventNotificationConfig.Interval();
        interval.setTime(10);
        interval.setMessage("Event starts in 10 minutes");

        when(eventNotificationConfig.getIntervals()).thenReturn(List.of(interval));

        Event event = new Event();
        event.setId(1L);
        event.setStartDate(LocalDateTime.now());
        event.setOwner(new User());
        event.setAttendees(List.of(new User()));

        when(eventService.getEventsStartingAt(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(event));
        when(eventService.claimEventNotification(any(Long.class), any(Integer.class))).thenReturn(true);

        eventScheduler.scheduleEventNotifications();

        verify(eventStartEventPublisher, times(1)).publishEvent(any(EventStartDto.class));
    }
}
