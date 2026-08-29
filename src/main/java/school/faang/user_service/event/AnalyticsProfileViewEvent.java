package school.faang.user_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnalyticsProfileViewEvent implements Event {
    @Builder.Default
    private int schemaVersion = 1;
    private String eventId;
    private Long userId;
    private Long viewerUserId;
    private Instant timestamp;
}
