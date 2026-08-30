package school.faang.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.entity.goal.GoalStatus;
import school.faang.user_service.filter.goalInvitation.InvitationFilter;
import school.faang.user_service.filter.goalInvitation.InvitationInvitedNameFilter;
import school.faang.user_service.filter.goalInvitation.InvitationInviterNameFilter;
import school.faang.user_service.filter.goalInvitation.InvitationInviterUserFilter;
import school.faang.user_service.mapper.GoalMapper;
import school.faang.user_service.repository.goal.GoalInvitationRepository;
import school.faang.user_service.service.goal.GoalInvitationService;
import school.faang.user_service.service.goal.GoalService;
import school.faang.user_service.service.user.UserService;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GoalInvitationServiceTest {

    @Mock
    private GoalInvitationRepository goalInvitationRepository;

    @Mock
    private GoalService goalService;

    @Mock
    private UserService userService;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private List<InvitationFilter> invitationFilters;

    @InjectMocks
    private GoalInvitationService goalInvitationService;

    private Goal goal;
    private User inviter;
    private User invited;
    private GoalInvitation goalInvitation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(goalInvitationService, "MAX_ACTIVE_GOALS_PER_USER", 3);

        goal = Goal.builder()
                .id(1L)
                .title("Learn Java")
                .description("Complete a Java course within 3 months")
                .status(GoalStatus.ACTIVE)
                .build();

        inviter = User.builder()
                .id(1L)
                .username("JohnDoe")
                .email("john.doe@example.com")
                .active(true)
                .build();

        invited = User.builder()
                .id(2L)
                .username("JaneSmith")
                .email("jane.smith@example.com")
                .active(true)
                .build();

        goalInvitation = GoalInvitation.builder()
                .goal(goal)
                .inviter(inviter)
                .invited(invited)
                .status(RequestStatus.PENDING)
                .build();
    }

    @Test
    public void testCreateInvitation_Success() {

        when(goalService.getGoalById(1L)).thenReturn(goal);
        when(userService.getUserById(1L)).thenReturn(inviter);
        when(userService.getUserById(2L)).thenReturn(invited);
        when(goalInvitationRepository.save(goalInvitation)).thenReturn(goalInvitation);

        GoalInvitation result = goalInvitationService.createInvitation(goalInvitation);

        assertNotNull(result);
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(inviter, result.getInviter());
        assertEquals(invited, result.getInvited());
        verify(goalInvitationRepository).save(goalInvitation);
    }

    @Test
    void testCreateInvitation_ThrowsExceptionWhenUsersAreSame() {
        goalInvitation.setInvited(inviter);
        goalInvitation.setInviter(inviter);

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.createInvitation(goalInvitation));
    }

    @Test
    void testCreateInvitation_ThrowsExceptionWhenUserIsNull() {
        inviter.setId(null);
        goalInvitation.setInviter(inviter);

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.createInvitation(goalInvitation));
    }

    @Test
    void testAcceptGoalInvitation_ThrowsExceptionWhenNoInvitationWithId() {
        long nonExistentId = 1L;

        when(goalInvitationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.acceptGoalInvitation(nonExistentId, 2L));

    }

    @Test
    void testAcceptGoalInvitation_ThrowsExceptionWhenUserExceedsGoalsLimit() {
        long invitationId = 1L;

        User user1 = User.builder()
                .id(1L)
                .build();

        Goal goal1 = Goal.builder()
                .id(1L)
                .users(List.of(user1))
                .build();

        Goal goal2 = Goal.builder()
                .id(2L)
                .users(List.of(user1))
                .build();

        Goal goal3 = Goal.builder()
                .id(3L)
                .users(List.of(user1))
                .build();


        User user = User.builder()
                .id(2L)
                .goals(Arrays.asList(goal1, goal2, goal3))
                .build();

        GoalInvitation goalInvitation = GoalInvitation.builder()
                .id(invitationId)
                .invited(user)
                .goal(goal2)
                .build();

        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(goalInvitation));

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.acceptGoalInvitation(invitationId, user.getId()));
    }


    @Test
    void testAcceptGoalInvitation_ThrowsExceptionWhenUserAlreadyContainsGoal() {
        long invitationId = 1L;

        User user = User.builder()
                .id(2L)
                .goals(Collections.singletonList(Goal.builder().id(1L).build()))
                .build();

        Goal goal = Goal.builder()
                .id(1L)
                .users(Collections.singletonList(user))
                .build();

        GoalInvitation goalInvitation = GoalInvitation.builder()
                .id(invitationId)
                .invited(user)
                .goal(goal)
                .build();

        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(goalInvitation));

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.acceptGoalInvitation(invitationId, user.getId()));
    }

    @Test
    void testRejectGoalInvitation_ThrowsExceptionWhenNoInvitationWithId() {
        long nonExistentId = 1L;

        when(goalInvitationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                goalInvitationService.rejectGoalInvitation(nonExistentId, 2L));
    }

    @Test
    void acceptGoalInvitation_updatesBothSidesAndInvitation() {
        long invitationId = 10L;
        User acceptingUser = User.builder().id(2L).goals(new ArrayList<>()).build();
        Goal invitedGoal = Goal.builder()
                .id(5L)
                .users(new ArrayList<>())
                .skillsToAchieve(new ArrayList<>())
                .build();
        GoalInvitation invitation = GoalInvitation.builder()
                .id(invitationId)
                .invited(acceptingUser)
                .goal(invitedGoal)
                .status(RequestStatus.PENDING)
                .build();
        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(goalMapper.mapSkills(invitedGoal.getSkillsToAchieve())).thenReturn(List.of());
        when(goalInvitationRepository.save(invitation)).thenReturn(invitation);

        GoalInvitation result = goalInvitationService.acceptGoalInvitation(invitationId, acceptingUser.getId());

        assertEquals(RequestStatus.ACCEPTED, result.getStatus());
        assertEquals(List.of(invitedGoal), acceptingUser.getGoals());
        assertEquals(List.of(acceptingUser), invitedGoal.getUsers());
        verify(userService).updateUser(acceptingUser);
        verify(goalService).updateGoal(invitedGoal.getId(), invitedGoal, null, List.of());
    }

    @Test
    void rejectGoalInvitation_marksInvitationRejected() {
        long invitationId = 11L;
        GoalInvitation invitation = GoalInvitation.builder()
                .id(invitationId)
                .invited(invited)
                .status(RequestStatus.PENDING)
                .build();
        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(goalInvitationRepository.save(invitation)).thenReturn(invitation);

        GoalInvitation result = goalInvitationService.rejectGoalInvitation(invitationId, invited.getId());

        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(goalInvitationRepository).save(invitation);
    }



    @Test
    public void testGetInvitations_Success() {

        GoalInvitation goalInvitation1 = GoalInvitation.builder()
                .id(2L)
                .goal(goal)
                .inviter(inviter)
                .invited(invited)
                .status(RequestStatus.PENDING)
                .build();

        InvitationFilter filterMock = mock(InvitationFilter.class);
        when(filterMock.isApplicable(any())).thenReturn(true);
        when(filterMock.apply(any(), any())).thenReturn(Stream.of(goalInvitation, goalInvitation1));

        when(invitationFilters.stream()).thenReturn(Stream.of(filterMock));
        when(goalInvitationRepository.findAll()).thenReturn(Arrays.asList(goalInvitation, goalInvitation1));

        InvitationFilterDto filterDto = new InvitationFilterDto();
        List<GoalInvitation> result = goalInvitationService.getInvitations(filterDto);

        assertEquals(2, result.size());
    }

    @Test
    void testGetInvitations_withFilters() {
        User user1 = User.builder().id(1L).username("JohnDoe").build();
        User user2 = User.builder().id(2L).username("JaneSmith").build();
        User user3 = User.builder().id(3L).username("MichaelJohnson").build();
        User user4 = User.builder().id(4L).username("EmilyDavis").build();

        GoalInvitation invitation1 = GoalInvitation.builder()
                        .id(1L)
                        .inviter(user1)
                        .invited(user2)
                        .status(RequestStatus.PENDING)
                        .build();

        GoalInvitation invitation2 = GoalInvitation.builder()
                        .id(2L)
                        .inviter(user1)
                        .invited(user3)
                        .status(RequestStatus.PENDING)
                        .build();

        GoalInvitation invitation3 = GoalInvitation.builder()
                .id(3L)
                .inviter(user4)
                .invited(user2)
                .status(RequestStatus.ACCEPTED)
                .build();

        when(goalInvitationRepository.findAll()).thenReturn(Arrays.asList(invitation1, invitation2, invitation3));

        List<InvitationFilter> mockFilters = Arrays.asList(
                new InvitationInvitedNameFilter(),
                new InvitationInviterNameFilter(),
                new InvitationInviterUserFilter(),
                new InvitationInviterNameFilter()
        );

        when(invitationFilters.stream()).thenReturn(mockFilters.stream());

        InvitationFilterDto filterDto = new InvitationFilterDto();
        filterDto.setInviterId(1L);
        filterDto.setInvitedId(2L);
        filterDto.setInviterNamePattern("John");
        filterDto.setInvitedNamePattern("Smith");

        List<GoalInvitation> result = goalInvitationService.getInvitations(filterDto);

        assertEquals(1, result.size());
        assertEquals(invitation1, result.get(0));
    }
}
