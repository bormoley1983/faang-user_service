package school.faang.user_service.service.subscription;

import org.springframework.data.util.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.user_service.entity.User;
import school.faang.user_service.exception.DataValidationException;
import school.faang.user_service.repository.SubscriptionRepository;
import school.faang.user_service.filter.user.UserFilter;
import school.faang.user_service.validation.subscription.SubscriptionValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionValidator subscriptionValidator;

    @Mock
    private List<UserFilter> userFilters;

    @Test
    public void testFollowUser_whenAlreadyFollowing_isIdempotentAndDoesNotThrow() {
        // Arrange: the conflict-safe insert reports 0 rows affected (duplicate)
        Pair<Long, Long> userIds = preparePairUserIds();

        when(subscriptionRepository
                .followUser(userIds.getFirst(), userIds.getSecond()))
                .thenReturn(0);

        // Act + Assert: no exception, stable domain outcome
        subscriptionService.followUser(userIds.getFirst(), userIds.getSecond());

        verify(subscriptionRepository, times(1))
                .followUser(userIds.getFirst(), userIds.getSecond());
    }

    @Test
    public void testFollowUser_whenNewSubscription_insertsRow() {
        // Arrange: the conflict-safe insert reports 1 row affected (new)
        Pair<Long, Long> userIds = preparePairUserIds();

        when(subscriptionRepository
                .followUser(userIds.getFirst(), userIds.getSecond()))
                .thenReturn(1);

        // Act
        subscriptionService.followUser(userIds.getFirst(), userIds.getSecond());

        // Assert
        verify(subscriptionRepository, times(1))
                .followUser(userIds.getFirst(), userIds.getSecond());
    }

    @Test
    public void testFollowUser_whenSelfSubscription_throwsValidation() {
        // Arrange: use a real validator so self-subscription is actually rejected
        SubscriptionService serviceWithRealValidator = new SubscriptionService(
                subscriptionRepository, new SubscriptionValidator(), userFilters);
        long selfId = 7L;

        // Act + Assert: validator rejects self-subscription before any insert
        assertThrows(DataValidationException.class,
                () -> serviceWithRealValidator.followUser(selfId, selfId));

        verify(subscriptionRepository, org.mockito.Mockito.never())
                .followUser(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    public void testUnfollowUserExistsByFollowerIdAndFolloweeId() {
        Pair<Long, Long> userIds = preparePairUserIds();

        when(subscriptionRepository
                .existsByFollowerIdAndFolloweeId(userIds.getFirst(), userIds.getSecond()))
                .thenReturn(false);

        assertThrows(DataValidationException.class,
                () -> subscriptionService
                        .unfollowUser(userIds.getFirst(), userIds.getSecond()));
    }

    @Test
    public void testUnfollowUser() {
        Pair<Long, Long> userIds = preparePairUserIds();

        when(subscriptionRepository
                .existsByFollowerIdAndFolloweeId(userIds.getFirst(), userIds.getSecond()))
                .thenReturn(true);

        subscriptionService.unfollowUser(userIds.getFirst(), userIds.getSecond());

        verify(subscriptionRepository, times(1))
                .unfollowUser(userIds.getFirst(), userIds.getSecond());
    }

    @Test
    public void testGetFollowers() {
        long followeeId = 1L;
        List<User> expectedFollowers = prepareExpectedUsers();

        when(subscriptionRepository.findByFolloweeId(followeeId))
                .thenReturn(expectedFollowers.stream());

        List<User> actualFollowers = subscriptionService.getFollowers(followeeId, null);

        verify(subscriptionRepository, times(1))
                .findByFolloweeId(followeeId);

        assertEquals(expectedFollowers.size(), actualFollowers.size());
        assertEquals(expectedFollowers, actualFollowers);
    }

    @Test
    public void testGetFollowersCount() {
        long followeeId = 1L;
        int expectedFollowersCount = 10;

        when(subscriptionRepository.findFollowersAmountByFolloweeId(followeeId))
                .thenReturn(expectedFollowersCount);

        int actualFollowersCount = subscriptionService.getFollowersCount(followeeId);

        verify(subscriptionRepository, times(1))
                .findFollowersAmountByFolloweeId(followeeId);

        assertEquals(expectedFollowersCount, actualFollowersCount);
    }

    @Test
    public void testGetFollowing() {
        long followerId = 1L;
        List<User> expectedFollowings = prepareExpectedUsers();

        when(subscriptionRepository.findByFollowerId(followerId))
                .thenReturn(expectedFollowings.stream());

        List<User> actualFollowings = subscriptionService.getFollowing(followerId, null);

        verify(subscriptionRepository, times(1))
                .findByFollowerId(followerId);

        assertEquals(expectedFollowings.size(), actualFollowings.size());
        assertEquals(expectedFollowings, actualFollowings);
    }

    @Test
    public void testGetFollowingCount() {
        long followerId = 1L;
        int expectedFollowingCount = 10;

        when(subscriptionRepository.findFolloweesAmountByFollowerId(followerId))
                .thenReturn(expectedFollowingCount);

        int actualFollowingCount = subscriptionService.getFollowingCount(followerId);

        verify(subscriptionRepository, times(1))
                .findFolloweesAmountByFollowerId(followerId);

        assertEquals(expectedFollowingCount, actualFollowingCount);
    }

    private Pair<Long, Long> preparePairUserIds() {
        return Pair.of(1L, 2L);
    }

    private List<User> prepareExpectedUsers() {
        User firstUser = new User();
        firstUser.setId(2L);
        User secondUser = new User();
        secondUser.setId(3L);
        return List.of(firstUser, secondUser);
    }
}
