package application.domain.usecases.ports;

import application.domain.entities.User;

public interface ChangeUserStatusUseCase {
  User changeUserStatus(String userId, User.Status status);
}
