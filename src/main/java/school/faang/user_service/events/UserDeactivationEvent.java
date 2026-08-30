package school.faang.user_service.events;

import faang.school.event.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDeactivationEvent implements Event {
    private long userId;
    private Instant timestamp;
}
