package school.faang.user_service.controller.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.dto.UserDto;
import school.faang.user_service.entity.User;
import school.faang.user_service.mapper.EventParticipantsMapper;
import school.faang.user_service.service.event.EventParticipationService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventParticipationControllerTest {

    @Mock
    private EventParticipationService eventParticipationService;

    @Mock
    private EventParticipantsMapper eventParticipantsMapper;

    @InjectMocks
    private EventParticipationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerParticipant_ShouldReturnCreated() {
        ResponseEntity<Void> response = controller.registerParticipant(1L, 2L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(eventParticipationService).registerParticipant(1L, 2L);
    }

    @Test
    void unregisterParticipant_ShouldReturnNoContent() {
        ResponseEntity<Void> response = controller.unregisterParticipant(1L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventParticipationService).unregisterParticipant(1L, 2L);
    }

    @Test
    void getParticipant_ShouldReturnMappedUsers() {
        User user = new User();
        UserDto dto = new UserDto();
        when(eventParticipationService.getParticipants(1L)).thenReturn(Collections.singletonList(user));
        when(eventParticipantsMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<List<UserDto>> response = controller.getParticipant(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(dto), response.getBody());
    }

    @Test
    void getParticipantsCount_ShouldReturnCount() {
        when(eventParticipationService.getParticipantsCount(1L)).thenReturn(7);

        ResponseEntity<Integer> response = controller.getParticipantsCount(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7, response.getBody());
    }
}
