package school.faang.user_service.filter.user;

import org.springframework.stereotype.Component;
import school.faang.user_service.dto.UserFilterDto;
import school.faang.user_service.entity.User;

@Component
public class UserCityFilter extends UserFilter {

    @Override
    public Object getFilterFieldValue(UserFilterDto filters) {
        return filters.getCityPattern();
    }

    @Override
    public boolean apply(User user, UserFilterDto filters) {
        return user.getCity() != null
                && filters.getCityPattern() != null
                && user.getCity().contains(filters.getCityPattern());
    }
}
