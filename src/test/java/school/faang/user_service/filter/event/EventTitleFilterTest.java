package school.faang.user_service.filter.event;

import org.junit.jupiter.api.Test;
import school.faang.user_service.dto.event.EventFilterDto;
import school.faang.user_service.entity.event.Event;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventTitleFilterTest {

    private final EventTitleFilter filter = new EventTitleFilter();

    @Test
    void isApplicable_whenTitleNull_returnsFalse() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();

        // Act
        boolean result = filter.isApplicable(dto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isApplicable_whenTitleBlank_returnsFalse() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();
        dto.setTitle("   ");

        // Act
        boolean result = filter.isApplicable(dto);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isApplicable_whenTitlePresent_returnsTrue() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();
        dto.setTitle("java");

        // Act
        boolean result = filter.isApplicable(dto);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void apply_whenTitleMatches_filtersCorrectly() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();
        dto.setTitle("java");
        Event match = Event.builder().title("Java Meetup").build();
        Event noMatch = Event.builder().title("Python Workshop").build();

        // Act
        long count = filter.apply(Stream.of(match, noMatch), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void apply_whenEventTitleNull_excludesIt() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();
        dto.setTitle("java");
        Event nullTitle = Event.builder().title(null).build();

        // Act
        long count = filter.apply(Stream.of(nullTitle), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    @Test
    void apply_whenCaseInsensitive_matches() {
        // Arrange
        EventFilterDto dto = new EventFilterDto();
        dto.setTitle("JAVA");
        Event event = Event.builder().title("java meetup").build();

        // Act
        long count = filter.apply(Stream.of(event), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }
}
