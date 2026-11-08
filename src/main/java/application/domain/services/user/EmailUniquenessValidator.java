package application.domain.services.user;

import application.domain.exceptions.DuplicateEmailException;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;

/** Single-responsibility guard for the e-mail uniqueness rule shared by all registrations. */
@Component
public class EmailUniquenessValidator {

  private final UserRepositoryPort userRepository;

  public EmailUniquenessValidator(UserRepositoryPort userRepository) {
    this.userRepository = userRepository;
  }

  public void ensureAvailable(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException(email);
    }
  }
}
