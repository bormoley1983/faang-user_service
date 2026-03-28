package school.faang.user_service.filter.goal;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.goal.GoalFilterDto;
import school.faang.user_service.entity.goal.Goal;

@Component
public class TitleGoalFilter implements GoalFilter {

    @Override
    public boolean isApplicable(GoalFilterDto goalFilterDto) {
        return goalFilterDto.getTitle() != null;
    }

    @Override
    public boolean apply(GoalFilterDto goalFilterDto, Goal goal) {
        return goal.getTitle() != null && goal.getTitle().contains(goalFilterDto.getTitle());
    }

}
