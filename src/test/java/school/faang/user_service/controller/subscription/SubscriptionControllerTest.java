package school.faang.user_service.controller.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.dto.SubscriptionUserDto;
import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.service.subscription.SubscriptionService;
import school.faang.user_service.validation.DtoValidator;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DtoValidator<SubscriptionUserDto> validator;

    @InjectMocks
    private SubscriptionController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void followUser_ShouldReturnOk() {
        ResponseEntity<Void> response = controller.followUser(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(subscriptionService).followUser(1L, 2L);
    }

    @Test
    void unfollowUser_ShouldReturnOk() {
        ResponseEntity<Void> response = controller.unfollowUser(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(subscriptionService).unfollowUser(1L, 2L);
    }

    @Test
    void getFollowers_ShouldValidateAndReturnMappedList() {
        UserFilterDto filter = new UserFilterDto();
        List<User> users = Collections.singletonList(new User());
        List<SubscriptionUserDto> dtos = Collections.singletonList(new SubscriptionUserDto());
        when(subscriptionService.getFollowers(1L, filter)).thenReturn(users);
        when(userMapper.toDto(users)).thenReturn(dtos);

        ResponseEntity<List<SubscriptionUserDto>> response = controller.getFollowers(1L, filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
        verify(validator).validate(dtos);
    }

    @Test
    void getFollowersCount_ShouldReturnCount() {
        when(subscriptionService.getFollowersCount(1L)).thenReturn(4);

        ResponseEntity<Integer> response = controller.getFollowersCount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody());
    }

    @Test
    void getFollowing_ShouldValidateAndReturnMappedList() {
        UserFilterDto filter = new UserFilterDto();
        List<User> users = Collections.singletonList(new User());
        List<SubscriptionUserDto> dtos = Collections.singletonList(new SubscriptionUserDto());
        when(subscriptionService.getFollowing(1L, filter)).thenReturn(users);
        when(userMapper.toDto(users)).thenReturn(dtos);

        ResponseEntity<List<SubscriptionUserDto>> response = controller.getFollowing(1L, filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
        verify(validator).validate(dtos);
    }

    @Test
    void getFollowingCount_ShouldReturnCount() {
        when(subscriptionService.getFollowingCount(1L)).thenReturn(6);

        ResponseEntity<Integer> response = controller.getFollowingCount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody());
    }
}
