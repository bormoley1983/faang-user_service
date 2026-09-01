package school.faang.user_service.controller.mentorship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.service.MentorshipService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MentorshipControllerTest {

    @Mock
    private MentorshipService mentorshipService;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private MentorshipController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userContext.getUserId()).thenReturn(1L);
    }

    @Test
    void getMentees_ShouldReturnMappedIds() {
        List<Long> menteeIds = List.of(2L, 3L);
        when(mentorshipService.getMentees(1L)).thenReturn(menteeIds);

        List<Long> result = controller.getMentees(1L);

        assertEquals(menteeIds, result);
    }

    @Test
    void getMentors_ShouldReturnMappedIds() {
        List<Long> mentorIds = List.of(4L);
        when(mentorshipService.getMentors(1L)).thenReturn(mentorIds);

        List<Long> result = controller.getMentors(1L);

        assertEquals(mentorIds, result);
    }

    @Test
    void deleteMentee_ShouldReturnSuccessMessage() {
        ResponseEntity<String> response = controller.deleteMentee(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The mentee was successfully deleted", response.getBody());
        verify(mentorshipService).deleteMentee(1L, 1L, 2L);
    }

    @Test
    void deleteMentor_ShouldReturnSuccessMessage() {
        ResponseEntity<String> response = controller.deleteMentor(1L, 3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The mentor was successfully deleted", response.getBody());
        verify(mentorshipService).deleteMentor(1L, 1L, 3L);
    }
}
