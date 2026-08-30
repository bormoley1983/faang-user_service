package school.faang.user_service.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.event.EventParticipationRepository;

import java.util.List;


@RequiredArgsConstructor
@Service
public class EventParticipationService {

    private final EventParticipationRepository eventParticipationRepository;

    @Transactional
    public void registerParticipant(long eventId, long userId) {
        if (eventParticipationRepository.existsUserByEventIdAndUserId(eventId, userId)) {
            throw new IllegalStateException(
                    String.format("User with ID %d is already registered for event with ID %d", userId, eventId)
            );
        }
        eventParticipationRepository.register(eventId, userId);
    }

    @Transactional
    public void unregisterParticipant(long eventId, long userId) {
        if (!eventParticipationRepository.existsUserByEventIdAndUserId(eventId, userId)) {
            throw new IllegalStateException(
                    String.format("User with ID %d is not registered for event with ID %d", userId, eventId)
            );
        }
        eventParticipationRepository.unregister(eventId, userId);
    }

    public List<User> getParticipants(long eventId) {
        return eventParticipationRepository.findAllParticipantsByEventId(eventId);
    }

    public int getParticipantsCount(long eventId) {
        return eventParticipationRepository.countParticipants(eventId);
    }
}
