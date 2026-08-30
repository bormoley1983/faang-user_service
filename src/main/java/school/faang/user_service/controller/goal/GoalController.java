package school.faang.user_service.controller.goal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.config.context.UserContext;
import school.faang.user_service.dto.goal.GoalDto;
import school.faang.user_service.dto.goal.GoalFilterDto;
import school.faang.user_service.entity.goal.Goal;
import school.faang.user_service.mapper.GoalMapper;
import school.faang.user_service.service.goal.GoalService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/goals")
@RestController
public class GoalController {

    private final GoalService goalService;
    private final GoalMapper goalMapper;
    private final UserContext userContext;

    @PostMapping("/{userId}")
    public ResponseEntity<GoalDto> createGoal(
            @PathVariable @Positive(message = "Please, provide positive user ID") Long userId,
            @RequestBody @Valid GoalDto goalDto) {

        if (!userId.equals(userContext.getUserId())) {
            throw new IllegalArgumentException(
                    String.format("User with id %s cannot create a goal for user with id %s",
                            userContext.getUserId(), userId));
        }

        Goal createdGoal = goalService.createGoal(userId,
                goalDto.getTitle(),
                goalDto.getDescription(),
                goalDto.getParentId(),
                Optional.ofNullable(goalDto.getSkillIds()).orElse(List.of())
        );
        GoalDto createdGoalDto = goalMapper.toDto(createdGoal);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(createdGoalDto);
    }

    @PatchMapping("/{goalId}")
    public ResponseEntity<GoalDto> updateGoal(
            @PathVariable @Positive(message = "Please, provide positive goal ID") Long goalId,
            @RequestBody @Valid GoalDto goalDto) {

        Goal updatedGoal = goalService.updateGoal(goalId, userContext.getUserId(), goalDto);
        GoalDto updatedGoalDto = goalMapper.toDto(updatedGoal);
        return ResponseEntity.status(HttpStatus.OK).
                body(updatedGoalDto);
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<String> deleteGoal(
            @PathVariable @Positive(message = "Please, provide positive goal ID") Long goalId) {

         goalService.deleteGoal(goalId, userContext.getUserId());
        return ResponseEntity.ok().body(String.format("Goal with id %s has been deleted successfully !", goalId));
    }

    @PostMapping("/parent-goal/{parentGoalId}/goals")
    public ResponseEntity<List<GoalDto>> findSubGoalsByParentIdWithFilter(
            @PathVariable @Positive(message = "Please, provide positive parent goal ID") Long parentGoalId,
            @RequestBody GoalFilterDto goalFilterDto) {

        List<Goal> goalsList = goalService.findSubGoalsByParentId(parentGoalId, goalFilterDto);
        List<GoalDto> goalsDtoList = goalMapper.toDtoList(goalsList);
        return ResponseEntity.status(HttpStatus.OK)
                .body(goalsDtoList);
    }

    @PostMapping("/user/{userId}/goals")
    public ResponseEntity<List<GoalDto>> findGoalsByUserIdWithFilter(
            @PathVariable @Positive(message = "Please, provide positive user ID") Long userId,
            @RequestBody GoalFilterDto goalFilterDto) {

        List<Goal> goalsList = goalService.findSubGoalsByUserId(userId, goalFilterDto);
        List<GoalDto> goalsDtoList = goalMapper.toDtoList(goalsList);
        return ResponseEntity.status(HttpStatus.OK)
                .body(goalsDtoList);
    }
}
