package school.faang.user_service.filter.user;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.User;

@Component
public class UserEmailFilter extends UserFilter {

    @Override
    public Object getFilterFieldValue(UserFilterDto filters) {
        return filters.getEmailPattern();
    }

    @Override
    public boolean apply(User user, UserFilterDto filters) {
        return user.getEmail() != null
                && filters.getEmailPattern() != null
                && user.getEmail().contains(filters.getEmailPattern());

    }
}
