package application.domain.usecases;

import application.domain.entities.User;
import application.domain.exceptions.UserNotFoundException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.usecases.ports.ChangeUserStatusUseCase;
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
