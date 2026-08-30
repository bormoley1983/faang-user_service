package school.faang.user_service.filter.event;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.entity.event.Event;

import java.util.Locale;
import java.util.stream.Stream;

@Component
public class EventTitleFilter implements EventFilter {
    @Override
    public boolean isApplicable(EventFilterDto filters) {
        return filters.getTitle() != null && !filters.getTitle().isBlank();
    }

    @Override
    public Stream<Event> apply(Stream<Event> events, EventFilterDto filters) {
        String needle = filters.getTitle().toLowerCase(Locale.ROOT);
        return events.filter(event -> event.getTitle() != null
                && event.getTitle().toLowerCase(Locale.ROOT).contains(needle));
    }
}
