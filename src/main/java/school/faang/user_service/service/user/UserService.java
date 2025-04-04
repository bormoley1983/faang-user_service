package school.faang.user_service.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public boolean isExists(long userId) {
        return userRepository.existsById(userId);
    }

    @Transactional
    public void banUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setBanned(true);
            userRepository.save(user);
        });
    }
}

