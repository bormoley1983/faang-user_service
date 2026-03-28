package school.faang.user_service.filter.user;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.User;

@Component
public class UserAboutFilter extends UserFilter {
    @Override
    public Object getFilterFieldValue(UserFilterDto filters) {
        return filters.getAboutPattern();
    }

    @Override
    public boolean apply(User user, UserFilterDto filters) {
        return user.getAboutMe() != null
                && filters.getAboutPattern() != null
                && user.getAboutMe().contains(filters.getAboutPattern());
    }
}
