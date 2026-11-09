package application.domain.usecases;

import application.domain.entities.User;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.usecases.ports.LoginUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {
  private final UserRepositoryPort userRepository;
  private final PasswordEncoder passwordEncoder;

  public LoginService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User authenticate(String email, String rawPassword) {
    User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    if (!user.isActive()) throw new InvalidCredentialsException();
    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
      throw new InvalidCredentialsException();
    return user;
  }
}
