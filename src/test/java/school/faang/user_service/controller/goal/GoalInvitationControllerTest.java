package school.faang.user_service.controller.goal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.mapper.GoalInvitationMapper;
import school.faang.user_service.service.goal.GoalInvitationService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoalInvitationControllerTest {

    @Mock
    private GoalInvitationService goalInvitationService;

    @Mock
    private GoalInvitationMapper goalInvitationMapper;

    @Mock
    private UserContext userContext;

    @InjectMocks
    private GoalInvitationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userContext.getUserId()).thenReturn(1L);
    }

    @Test
    void createInvitation_ShouldReturnCreatedDto() {
        GoalInvitationDto in = new GoalInvitationDto();
        GoalInvitation entity = new GoalInvitation();
        GoalInvitation created = new GoalInvitation();
        GoalInvitationDto out = new GoalInvitationDto();
        when(goalInvitationMapper.toEntity(in)).thenReturn(entity);
        when(goalInvitationService.createInvitation(entity)).thenReturn(created);
        when(goalInvitationMapper.toDto(created)).thenReturn(out);

        ResponseEntity<GoalInvitationDto> response = controller.createInvitation(in);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
    }

    @Test
    void acceptGoalInvitation_ShouldUseActingUser() {
        GoalInvitation updated = new GoalInvitation();
        GoalInvitationDto out = new GoalInvitationDto();
        when(goalInvitationService.acceptGoalInvitation(10L, 1L)).thenReturn(updated);
        when(goalInvitationMapper.toDto(updated)).thenReturn(out);

        ResponseEntity<GoalInvitationDto> response = controller.acceptGoalInvitation(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
        verify(goalInvitationService).acceptGoalInvitation(10L, 1L);
    }

    @Test
    void rejectGoalInvitation_ShouldUseActingUser() {
        GoalInvitation updated = new GoalInvitation();
        GoalInvitationDto out = new GoalInvitationDto();
        when(goalInvitationService.rejectGoalInvitation(10L, 1L)).thenReturn(updated);
        when(goalInvitationMapper.toDto(updated)).thenReturn(out);

        ResponseEntity<GoalInvitationDto> response = controller.rejectGoalInvitation(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(out, response.getBody());
        verify(goalInvitationService).rejectGoalInvitation(10L, 1L);
    }

    @Test
    void getInvitations_ShouldReturnMappedList() {
        InvitationFilterDto filter = new InvitationFilterDto();
        List<GoalInvitation> entities = Collections.singletonList(new GoalInvitation());
        List<GoalInvitationDto> dtos = Collections.singletonList(new GoalInvitationDto());
        when(goalInvitationService.getInvitations(filter)).thenReturn(entities);
        when(goalInvitationMapper.toDtoList(entities)).thenReturn(dtos);

        ResponseEntity<List<GoalInvitationDto>> response = controller.getInvitations(filter);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
    }
}
