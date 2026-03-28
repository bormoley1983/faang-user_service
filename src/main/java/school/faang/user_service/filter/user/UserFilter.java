package school.faang.user_service.filter.user;

import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.User;


public abstract class UserFilter {
    public boolean isApplicable(UserFilterDto filters) {
        return filters != null && getFilterFieldValue(filters) != null;
    }

    public abstract Object getFilterFieldValue(UserFilterDto filters);

    public abstract boolean apply(User user, UserFilterDto filters);
}
