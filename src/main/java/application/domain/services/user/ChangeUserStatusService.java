package application.domain.services.user;

import application.domain.exceptions.UserNotFoundException;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

  private final UserRepositoryPort userRepository;

  public ChangeUserStatusService(UserRepositoryPort userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public User changeUserStatus(String userId, User.Status status) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    switch (status) {
      case ACTIVE -> user.activate();
      case BLOCKED -> user.block();
      case INACTIVE -> user.deactivate();
    }
    return userRepository.save(user);
  }
}
