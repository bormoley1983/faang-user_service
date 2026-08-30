package school.faang.user_service.service.goal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.filter.goalInvitation.InvitationFilter;
import school.faang.user_service.mapper.GoalMapper;
import school.faang.user_service.repository.goal.GoalInvitationRepository;
import school.faang.user_service.service.user.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalInvitationService {
    private final GoalInvitationRepository goalInvitationRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final GoalMapper goalMapper;
    private final List<InvitationFilter> invitationFilters;
    @Value("${goal.max-active-goals-per-user}")
    private Integer MAX_ACTIVE_GOALS_PER_USER;

    @Transactional
    public GoalInvitation createInvitation(GoalInvitation goalInvitation) {
        Long goalId = goalInvitation.getGoal().getId();
        Long inviterId = goalInvitation.getInviter().getId();
        Long invitedId = goalInvitation.getInvited().getId();

        if (inviterId == null || invitedId == null) {
            log.error("Inviter or invited user don't exist. Inviterid: {}. InvitedId: {}", inviterId, invitedId);
            throw new IllegalArgumentException("There is no inviter or invited user. InviterId: " + inviterId + " InvitedId: " + invitedId);
        }
        if (Objects.equals(invitedId, inviterId)) {
            log.error("Inviter or invited user don't exist. Inviterid: {}. InvitedId: {}", inviterId, invitedId);
            throw new IllegalArgumentException("Inviter and invited user have the same id. InviterId: " + inviterId + " InvitedId: " + invitedId);
        }

        Goal goal = goalService.getGoalById(goalId);
        User inviter = userService.getUserById(inviterId);
        User invited = userService.getUserById(invitedId);

        goalInvitation.setGoal(goal);
        goalInvitation.setInviter(inviter);
        goalInvitation.setInvited(invited);
        goalInvitation.setStatus(RequestStatus.PENDING);

        GoalInvitation createdInvitation = goalInvitationRepository.save(goalInvitation);

        log.info("created new goal invitation with id: {}", goalInvitation.getId());
        return createdInvitation;
    }

    @Transactional
    public GoalInvitation acceptGoalInvitation(long goalInvitationId, long actingUserId) {
        GoalInvitation goalInvitation = goalInvitationRepository.findById(goalInvitationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("There is no invitation with id: " + goalInvitationId));
        User user = goalInvitation.getInvited();

        if (!Objects.equals(user.getId(), actingUserId)) {
            log.error("User with id {} tried to accept invitation {} addressed to user {}",
                    actingUserId, goalInvitationId, user.getId());
            throw new IllegalArgumentException(
                    "Only the invited user (id " + user.getId() + ") can accept this invitation");
        }

        List<Goal> userGoals = user.getGoals();
        Goal goal = goalInvitation.getGoal();
        List<User> goalUsers = goal.getUsers();

        if (userGoals.size() >= MAX_ACTIVE_GOALS_PER_USER) {
            log.error("User with id: {} already have more than {} active goals", user.getId(), MAX_ACTIVE_GOALS_PER_USER);
            throw new IllegalArgumentException("User already has limit of goals. Limit is " + MAX_ACTIVE_GOALS_PER_USER);
        }
        if (goalUsers.contains(user)) {
            log.error("User with id: {}. Already working on goal with id: {}", user.getId(), goal.getId());
            throw new IllegalArgumentException("User with id = " + user.getId() + " is already working on goal with id = " + goal.getId());
        }

        goalInvitation.setStatus(RequestStatus.ACCEPTED);
        user.getGoals().add(goal);
        goal.getUsers().add(user);

        userService.updateUser(user);

        List<Long> skillIds = goalMapper.mapSkills(goal.getSkillsToAchieve());
        Long parentId = goal.getParent() != null ? goal.getParent().getId() : null;
        goalService.updateGoal(goal.getId(), goal, parentId, skillIds);
        GoalInvitation updated = goalInvitationRepository.save(goalInvitation);

        log.info("goal invitation with id: {}. Was accepted by user with id: {}", goalInvitation.getId(), user.getId());
        return updated;
    }

    @Transactional
    public GoalInvitation rejectGoalInvitation(long goalInvitationId, long actingUserId) {
        GoalInvitation goalInvitation = goalInvitationRepository.findById(goalInvitationId)
                .orElseThrow(() -> new IllegalArgumentException("There is no invitation with id = " + goalInvitationId));

        // Only the invited user may reject their own invitation.
        if (!Objects.equals(goalInvitation.getInvited().getId(), actingUserId)) {
            log.error("User with id {} tried to reject invitation {} addressed to user {}",
                    actingUserId, goalInvitationId, goalInvitation.getInvited().getId());
            throw new IllegalArgumentException(
                    "Only the invited user (id " + goalInvitation.getInvited().getId() + ") can reject this invitation");
        }

        goalInvitation.setStatus(RequestStatus.REJECTED);

        GoalInvitation updated = goalInvitationRepository.save(goalInvitation);

        log.info("goal invitation with id: {}. Was rejected by user with id: {}", goalInvitation.getId(), goalInvitation.getInvited().getId());
        return updated;
    }

    public List<GoalInvitation> getInvitations(InvitationFilterDto invitationFilter) {
        Stream<GoalInvitation> invitations = goalInvitationRepository.findAll().stream();

        List<InvitationFilter> applicable =  invitationFilters.stream()
                .filter((filter) -> filter.isApplicable(invitationFilter))
                .toList();

        for (InvitationFilter filter : applicable) {
            invitations = filter.apply(invitations, invitationFilter);
        }

        return invitations.toList();
    }

}
