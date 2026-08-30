package school.faang.user_service.controller.goal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.goal.GoalInvitationDto;
import school.faang.user_service.dto.goal.InvitationFilterDto;
import school.faang.user_service.entity.goal.GoalInvitation;
import school.faang.user_service.mapper.GoalInvitationMapper;
import school.faang.user_service.service.goal.GoalInvitationService;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/goalInvitations")
@RestController
public class GoalInvitationController {

    private final GoalInvitationService goalInvitationService;
    private final GoalInvitationMapper goalInvitationMapper;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<GoalInvitationDto> createInvitation(@RequestBody @Valid GoalInvitationDto invitation) {
        GoalInvitation goalInvitation = goalInvitationMapper.toEntity(invitation);
        GoalInvitation createdInvitation = goalInvitationService.createInvitation(goalInvitation);
        return ResponseEntity.ok(goalInvitationMapper.toDto(createdInvitation));
    }

    @PostMapping("/{invitationId}")
    public ResponseEntity<GoalInvitationDto> acceptGoalInvitation(@PathVariable @Positive(message = "Invitation ID must be a positive number.") long invitationId) {
         GoalInvitation updatedGoalInvitation = goalInvitationService.acceptGoalInvitation(invitationId, userContext.getUserId());
        return ResponseEntity.ok(goalInvitationMapper.toDto(updatedGoalInvitation));
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<GoalInvitationDto> rejectGoalInvitation(@PathVariable @Positive(message = "Invitation ID must be a positive number.") long invitationId) {
        GoalInvitation updatedgoalInvitation = goalInvitationService.rejectGoalInvitation(invitationId, userContext.getUserId());
        return ResponseEntity.ok(goalInvitationMapper.toDto(updatedgoalInvitation));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<GoalInvitationDto>> getInvitations(@RequestBody InvitationFilterDto filter) {
        List<GoalInvitation> filtered = goalInvitationService.getInvitations(filter);
        return ResponseEntity.ok(goalInvitationMapper.toDtoList(filtered));
    }
}

