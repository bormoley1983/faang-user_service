package school.faang.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.entity.User;
import school.faang.user_service.repository.UserRepository;
import school.faang.user_service.repository.mentorship.MentorshipRepository;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MentorshipService {
    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;

    public void stopUserMentorship(Long userId) {
        mentorshipRepository.findById(userId).ifPresent(user -> {
            if (user.getMentees() != null) {
                user.getMentees().forEach(mentee -> {
                    removeMentorFromMentees(mentee);
                    removeMentorFromGoals(mentee, userId);
                });
            }
        });
    }

    private void removeMentorFromMentees(User mentee) {
        mentee.setMentors(mentee.getMentors().stream()
                .filter(mentor -> !Objects.equals(mentor.getId(), mentee.getId()))
                .toList());
        userRepository.save(mentee);
    }

    private void removeMentorFromGoals(User mentee, Long userId) {
        mentee.setGoals(mentee.getGoals().stream()
                .filter(goal -> Objects.equals(goal.getMentor().getId(), userId))
                .peek(goal -> goal.setMentor(mentee))
                .toList());
        userRepository.save(mentee);
    }


    @Transactional(readOnly = true)
    public List<Long> getMentees(Long userId) {
        return mentorshipRepository.findMenteeIdsByMentorId(userId);
    }

    @Transactional(readOnly = true)
    public List<Long> getMentors(Long userId) {
        return mentorshipRepository.findMentorIdsByMenteeId(userId);
    }

    @Transactional
    public void deleteMentee(Long actingUserId, Long mentorId, Long menteeId) {
        if (!Objects.equals(actingUserId, mentorId) && !Objects.equals(actingUserId, menteeId)) {
            throw new IllegalArgumentException(
                    String.format("User with id %s is not a party of mentorship between %s and %s",
                            actingUserId, mentorId, menteeId));
        }
        mentorshipRepository.deleteByMentorIdAndMenteeId(mentorId, menteeId);
    }

    @Transactional
    public void deleteMentor(Long actingUserId, Long menteeId, Long mentorId) {
        if (!Objects.equals(actingUserId, mentorId) && !Objects.equals(actingUserId, menteeId)) {
            throw new IllegalArgumentException(
                    String.format("User with id %s is not a party of mentorship between %s and %s",
                            actingUserId, mentorId, menteeId));
        }
        mentorshipRepository.deleteByMenteeIdAndMentorId(menteeId, mentorId);
    }
}
