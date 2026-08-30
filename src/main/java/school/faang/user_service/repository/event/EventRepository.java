package school.faang.user_service.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import school.faang.user_service.entity.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM event e
            WHERE e.user_id = :userId
            """)
    List<Event> findAllByUserId(long userId);

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM event e
            JOIN user_event ue ON ue.event_id = e.id
            WHERE ue.user_id = :userId
            """)
    List<Event> findParticipatedEventsByUserId(long userId);

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM event e
            WHERE e.start_date >= :startTime AND e.start_date < :doubleStartTime
            """)
    List<Event> findEventsByStartTime(LocalDateTime startTime, LocalDateTime doubleStartTime);

    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO event_notification_sent (event_id, interval_minutes)
            VALUES (:eventId, :intervalMinutes)
            ON CONFLICT (event_id, interval_minutes) DO NOTHING
            """)
    int claimEventNotification(long eventId, int intervalMinutes);

    @Query(nativeQuery = true, value = """
            WITH to_delete AS (
                SELECT * FROM event
                WHERE end_date < NOW()
                FOR UPDATE
            ),
            deleted AS (
                DELETE FROM event
                WHERE id IN (SELECT id FROM to_delete)
                RETURNING *
            )
            SELECT count(*) FROM deleted
            """)
    int deleteAllEndedInPast();
}