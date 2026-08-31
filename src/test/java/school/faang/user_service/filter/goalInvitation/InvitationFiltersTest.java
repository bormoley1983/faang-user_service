package school.faang.user_service.filter.goalInvitation;

import org.junit.jupiter.api.Test;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.RequestStatus;
import school.faang.user_service.entity.User;
import school.faang.user_service.entity.goal.GoalInvitation;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationFiltersTest {

    // --- InvitationInvitedNameFilter ---

    private final InvitationInvitedNameFilter invitedNameFilter = new InvitationInvitedNameFilter();

    @Test
    void invitedName_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(invitedNameFilter.isApplicable(new InvitationFilterDto(null, null, null, null, null))).isFalse();
    }

    @Test
    void invitedName_isApplicable_whenPatternPresent_returnsTrue() {
        InvitationFilterDto dto = new InvitationFilterDto(null, "john", null, null, null);
        assertThat(invitedNameFilter.isApplicable(dto)).isTrue();
    }

    @Test
    void invitedName_apply_whenUsernameMatches_keepsInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, "john", null, null, null);
        User invited = User.builder().id(1L).username("john_doe").build();
        GoalInvitation match = GoalInvitation.builder().invited(invited).build();

        // Act
        long count = invitedNameFilter.apply(Stream.of(match), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void invitedName_apply_whenUsernameDoesNotMatch_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, "john", null, null, null);
        User invited = User.builder().id(1L).username("jane_doe").build();
        GoalInvitation noMatch = GoalInvitation.builder().invited(invited).build();

        // Act
        long count = invitedNameFilter.apply(Stream.of(noMatch), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    @Test
    void invitedName_apply_whenInvitedNull_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, "john", null, null, null);
        GoalInvitation noInvited = GoalInvitation.builder().invited(null).build();

        // Act
        long count = invitedNameFilter.apply(Stream.of(noInvited), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    // --- InvitationInvitedUserFilter ---

    private final InvitationInvitedUserFilter invitedUserFilter = new InvitationInvitedUserFilter();

    @Test
    void invitedUser_isApplicable_whenIdNull_returnsFalse() {
        assertThat(invitedUserFilter.isApplicable(new InvitationFilterDto(null, null, null, null, null))).isFalse();
    }

    @Test
    void invitedUser_isApplicable_whenIdPresent_returnsTrue() {
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, 5L, null);
        assertThat(invitedUserFilter.isApplicable(dto)).isTrue();
    }

    @Test
    void invitedUser_apply_whenIdMatches_keepsInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, 5L, null);
        User invited = User.builder().id(5L).username("john").build();
        GoalInvitation match = GoalInvitation.builder().invited(invited).build();

        // Act
        long count = invitedUserFilter.apply(Stream.of(match), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void invitedUser_apply_whenIdDoesNotMatch_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, 5L, null);
        User invited = User.builder().id(6L).username("john").build();
        GoalInvitation noMatch = GoalInvitation.builder().invited(invited).build();

        // Act
        long count = invitedUserFilter.apply(Stream.of(noMatch), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    // --- InvitationInviterNameFilter ---

    private final InvitationInviterNameFilter inviterNameFilter = new InvitationInviterNameFilter();

    @Test
    void inviterName_isApplicable_whenPatternNull_returnsFalse() {
        assertThat(inviterNameFilter.isApplicable(new InvitationFilterDto(null, null, null, null, null))).isFalse();
    }

    @Test
    void inviterName_apply_whenUsernameMatches_keepsInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto("alice", null, null, null, null);
        User inviter = User.builder().id(1L).username("alice_smith").build();
        GoalInvitation match = GoalInvitation.builder().inviter(inviter).build();

        // Act
        long count = inviterNameFilter.apply(Stream.of(match), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void inviterName_apply_whenUsernameDoesNotMatch_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto("alice", null, null, null, null);
        User inviter = User.builder().id(1L).username("bob_smith").build();
        GoalInvitation noMatch = GoalInvitation.builder().inviter(inviter).build();

        // Act
        long count = inviterNameFilter.apply(Stream.of(noMatch), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    // --- InvitationInviterUserFilter ---

    private final InvitationInviterUserFilter inviterUserFilter = new InvitationInviterUserFilter();

    @Test
    void inviterUser_isApplicable_whenIdNull_returnsFalse() {
        assertThat(inviterUserFilter.isApplicable(new InvitationFilterDto(null, null, null, null, null))).isFalse();
    }

    @Test
    void inviterUser_apply_whenIdMatches_keepsInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, 3L, null, null);
        User inviter = User.builder().id(3L).username("alice").build();
        GoalInvitation match = GoalInvitation.builder().inviter(inviter).build();

        // Act
        long count = inviterUserFilter.apply(Stream.of(match), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void inviterUser_apply_whenIdDoesNotMatch_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, 3L, null, null);
        User inviter = User.builder().id(4L).username("alice").build();
        GoalInvitation noMatch = GoalInvitation.builder().inviter(inviter).build();

        // Act
        long count = inviterUserFilter.apply(Stream.of(noMatch), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    // --- InvitationStatusFilter ---

    private final InvitationStatusFilter statusFilter = new InvitationStatusFilter();

    @Test
    void status_isApplicable_whenStatusNull_returnsFalse() {
        assertThat(statusFilter.isApplicable(new InvitationFilterDto(null, null, null, null, null))).isFalse();
    }

    @Test
    void status_isApplicable_whenStatusPresent_returnsTrue() {
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, null, RequestStatus.PENDING);
        assertThat(statusFilter.isApplicable(dto)).isTrue();
    }

    @Test
    void status_apply_whenStatusMatches_keepsInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, null, RequestStatus.PENDING);
        GoalInvitation match = GoalInvitation.builder().status(RequestStatus.PENDING).build();

        // Act
        long count = statusFilter.apply(Stream.of(match), dto).count();

        // Assert
        assertThat(count).isEqualTo(1);
    }

    @Test
    void status_apply_whenStatusDoesNotMatch_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, null, RequestStatus.PENDING);
        GoalInvitation noMatch = GoalInvitation.builder().status(RequestStatus.ACCEPTED).build();

        // Act
        long count = statusFilter.apply(Stream.of(noMatch), dto).count();

        // Assert
        assertThat(count).isZero();
    }

    @Test
    void status_apply_whenInvitationStatusNull_excludesInvitation() {
        // Arrange
        InvitationFilterDto dto = new InvitationFilterDto(null, null, null, null, RequestStatus.PENDING);
        GoalInvitation noStatus = GoalInvitation.builder().status(null).build();

        // Act
        long count = statusFilter.apply(Stream.of(noStatus), dto).count();

        // Assert
        assertThat(count).isZero();
    }
}
