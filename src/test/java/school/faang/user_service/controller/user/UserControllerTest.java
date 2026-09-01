package school.faang.user_service.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.dto.avatar.AvatarType;
import school.faang.user_service.dto.user.UserRegistrationDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.UserMapper;
import school.faang.user_service.service.user.UserAvatarService;
import school.faang.user_service.service.user.UserService;

import java.net.URL;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserAvatarService userAvatarService;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userContext.getUserId()).thenReturn(1L);
    }

    @Test
    void getUser_ShouldReturnMappedDto() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userService.getUser(5L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<UserDto> response = controller.getUser(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void updateTelegramUserId_ShouldUseActingUserAndReturnDto() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userService.updateTelegramData(1L, "tg", "chat")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = controller.updateTelegramUserId("tg", "chat");

        assertEquals(dto, result);
    }

    @Test
    void bindTelegramChat_ShouldReturnMappedDto() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userService.bindTelegramChat(1L, 9L, "chat")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<UserDto> response = controller.bindTelegramChat(9L, "chat");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getUsersByIds_ShouldReturnMappedList() {
        List<Long> ids = List.of(1L, 2L);
        List<User> users = Collections.singletonList(new User());
        List<UserDto> dtos = Collections.singletonList(new UserDto());
        when(userService.getUsersByIds(ids)).thenReturn(users);
        when(userMapper.toUserDtoList(users)).thenReturn(dtos);

        ResponseEntity<List<UserDto>> response = controller.getUsersByIds(ids);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }

    @Test
    void deactivateUser_ShouldReturnOk() {
        ResponseEntity<Void> response = controller.deactivateUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).deactivateUser(1L, 1L);
    }

    @Test
    void registerUser_ShouldReturnRegistrationDto() {
        UserRegistrationDto in = new UserRegistrationDto();
        in.setUsername("u");
        in.setEmail("e@x.com");
        in.setPassword("p");
        in.setCountryId(1L);
        in.setTelegramUsername("tg");
        User registered = new User();
        UserRegistrationDto out = new UserRegistrationDto();
        when(userService.registerUser("u", "e@x.com", "p", 1L, "tg")).thenReturn(registered);
        when(userMapper.toRegistrationDto(registered)).thenReturn(out);

        ResponseEntity<UserRegistrationDto> response = controller.registerUser(in);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void generateAvatarForUser_ShouldReturnSuccessMessage() {
        User user = new User();
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.getUser(1L)).thenReturn(user);

        ResponseEntity<String> response = controller.generateAvatarForUser(AvatarType.PNG);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Avatar generated successfully.", response.getBody());
        verify(userAvatarService).generateAvatarForNewUser(user, AvatarType.PNG);
    }

    @Test
    void getUserAvatar_ShouldReturnAvatarUrl() throws Exception {
        User user = new User();
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.getUser(1L)).thenReturn(user);
        when(userAvatarService.getUserAvatar(user)).thenReturn(new URL("http://avatar"));

        ResponseEntity<String> response = controller.getUserAvatar();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://avatar", response.getBody());
    }

    @Test
    void uploadAvatar_ShouldReturnUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        when(userService.uploadAvatar(file, "small")).thenReturn("http://uploaded");

        ResponseEntity<String> response = controller.uploadAvatar(file, "small");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://uploaded", response.getBody());
    }

    @Test
    void downloadAvatar_ShouldReturnUrl() {
        when(userService.downloadAvatar("large")).thenReturn("http://downloaded");

        ResponseEntity<String> response = controller.downloadAvatar("large");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://downloaded", response.getBody());
    }

    @Test
    void deleteAvatar_ShouldReturnNoContent() {
        ResponseEntity<Void> response = controller.deleteAvatar();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteAvatar();
    }

    @Test
    void getUsersByIdsPaged_ShouldReturnPage() {
        List<Long> ids = List.of(1L);
        Page<UserDto> page = new PageImpl<>(Collections.singletonList(new UserDto()));
        when(userService.getUsersByIds(ids, PageRequest.of(0, 10))).thenReturn(page);

        Page<UserDto> result = controller.getUsersByIds(ids, PageRequest.of(0, 10));

        assertEquals(page, result);
    }
}
