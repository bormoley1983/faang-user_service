package school.faang.user_service.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.faang.user_service.config.scheduler.EventStartEventNotificationConfig;
import school.faang.user_service.dto.event.EventStartDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.event.Event;
import school.faang.user_service.publisher.event.NotificationEventStartEventPublisher;
import school.faang.user_service.service.event.EventService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventScheduler {
    private final EventService eventService;
    private final NotificationEventStartEventPublisher eventStartEventPublisher;
    private final EventStartEventNotificationConfig eventNotificationConfig;
    private final Environment environment;

    @PostConstruct
    private void init() {
        log.info("Event scheduler initialized with frequency: {}", environment.getProperty("event.removal.cron"));
    }

    @Scheduled(cron = "${event.removal.cron}")
    public void clearEvents() {
        int countEvents = eventService.clearEvents();
        log.info("Deleted {} events", countEvents);
    }

    @Scheduled(cron = "${event.start-notification.cron}")
    public void scheduleEventNotifications() {
        LocalDateTime now = LocalDateTime.now();
        // Intervals are processed from the largest to the smallest. Each event is notified at most
        // once per run: an event whose start falls into a larger interval's window is skipped when
        // the smaller interval's (overlapping) window is processed, so no duplicate notifications.
        List<EventStartEventNotificationConfig.Interval> intervals = eventNotificationConfig.getIntervals().stream()
                .sorted((a, b) -> Integer.compare(b.getTime(), a.getTime()))
                .toList();

        for (EventStartEventNotificationConfig.Interval interval : intervals) {
            LocalDateTime windowStart = now.plusMinutes(interval.getTime());
            LocalDateTime windowEnd = now.plusMinutes(interval.getTime() * 2L);
            scheduleNotificationsForTimeFrame(
                    windowStart,
                    windowEnd,
                    interval.getMessage(),
                    interval.getTime(),
                    intervals,
                    now
            );
        }
    }

    private void scheduleNotificationsForTimeFrame(LocalDateTime windowStart, LocalDateTime windowEnd,
                                                   String message,
                                                   int intervalMinutes,
                                                   List<EventStartEventNotificationConfig.Interval> allIntervals,
                                                   LocalDateTime now) {
        List<Event> events = eventService.getEventsStartingAt(windowStart, windowEnd);

        for (Event event : events) {
            // Skip events already covered by a larger interval processed earlier in this run.
            boolean coveredByLargerInterval = allIntervals.stream()
                    .filter(i -> i.getTime() > intervalMinutes)
                    .anyMatch(i -> isWithinWindow(event.getStartDate(),
                            now.plusMinutes(i.getTime()),
                            now.plusMinutes(i.getTime() * 2L)));
            if (coveredByLargerInterval) {
                continue;
            }

            if (!eventService.claimEventNotification(event.getId(), intervalMinutes)) {
                log.debug("Notification for event {} at interval {} min already claimed; skipping",
                        event.getId(), intervalMinutes);
                continue;
            }

            EventStartDto eventStartDto = EventStartDto.builder()
                    .eventId(event.getId())
                    .ownerId(event.getOwner().getId())
                    .userIds(event.getAttendees().stream().map(User::getId).toList())
                    .startTime(event.getStartDate())
                    .message(message)
                    .build();
            eventStartEventPublisher.publishEvent(eventStartDto);
            log.info("Scheduled notification for event {} with message: {}", event.getId(), message);
        }
    }

    private boolean isWithinWindow(LocalDateTime eventStart, LocalDateTime windowStart, LocalDateTime windowEnd) {
        return eventStart != null && !eventStart.isBefore(windowStart) && eventStart.isBefore(windowEnd);
    }
}
