package application.domain.usecases;

import application.domain.entities.Buyer;
import application.domain.entities.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.usecases.ports.RegisterBuyerUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterBuyerService implements RegisterBuyerUseCase {
  private final UserRepositoryPort userRepository;
  private final BuyerRepositoryPort buyerRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailUniquenessValidator emailUniquenessValidator;

  public RegisterBuyerService(
      UserRepositoryPort userRepository,
      BuyerRepositoryPort buyerRepository,
      PasswordEncoder passwordEncoder,
      EmailUniquenessValidator emailUniquenessValidator) {
    this.userRepository = userRepository;
    this.buyerRepository = buyerRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailUniquenessValidator = emailUniquenessValidator;
  }

  @Override
  @Transactional
  public User execute(
      String fullName,
      String email,
      String rawPassword,
      String identityDocument,
      String primaryAddress) {
    emailUniquenessValidator.ensureAvailable(email);
    String hash = passwordEncoder.encode(rawPassword);
    User user = User.create(fullName, email, hash, User.Role.BUYER, identityDocument);
    user = userRepository.save(user);
    buyerRepository.save(Buyer.create(user.getId(), primaryAddress));
    return user;
  }
}
