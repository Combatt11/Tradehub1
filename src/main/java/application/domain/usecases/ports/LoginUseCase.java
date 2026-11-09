package application.domain.usecases.ports;

import application.domain.entities.User;

public interface LoginUseCase {
  User authenticate(String email, String password);
}
