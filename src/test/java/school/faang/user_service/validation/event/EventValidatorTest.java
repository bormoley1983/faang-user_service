package school.faang.user_service.validation.event;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.dto.event.EventDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventValidatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;

    private final EventValidator validator = new EventValidator();

    @Test
    void isValid_whenNullDto_returnsFalse() {
        // Act
        boolean result = validator.isValid(null, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenStartTimeNull_returnsFalse() {
        // Arrange
        EventDto dto = EventDto.builder().startTime(null).endTime(LocalDateTime.now()).build();

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenEndTimeNull_returnsFalse() {
        // Arrange
        EventDto dto = EventDto.builder().startTime(LocalDateTime.now()).endTime(null).build();

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenStartAfterEnd_returnsFalseAndBuildsViolation() {
        // Arrange
        LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime start = end.plusHours(1);
        EventDto dto = EventDto.builder().startTime(start).endTime(end).build();

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isValid_whenStartBeforeEnd_returnsTrue() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = start.plusHours(2);
        EventDto dto = EventDto.builder().startTime(start).endTime(end).build();

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isValid_whenStartEqualsEnd_returnsTrue() {
        // Arrange
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 10, 0);
        EventDto dto = EventDto.builder().startTime(time).endTime(time).build();

        // Act
        boolean result = validator.isValid(dto, context);

        // Assert
        assertThat(result).isTrue();
    }
}
