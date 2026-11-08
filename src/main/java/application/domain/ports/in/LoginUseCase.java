package application.domain.ports.in;

import application.domain.models.User;

public interface LoginUseCase {
  User authenticate(String email, String password);
}
