package application.domain.services.seller;

import application.domain.exceptions.InvalidPasswordException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.in.RegisterSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.EmailUniquenessValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSellerService implements RegisterSellerUseCase {
  private final UserRepositoryPort userRepository;
  private final SellerRepositoryPort sellerRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailUniquenessValidator emailUniquenessValidator;

  public RegisterSellerService(
      UserRepositoryPort userRepository,
      SellerRepositoryPort sellerRepository,
      PasswordEncoder passwordEncoder,
      EmailUniquenessValidator emailUniquenessValidator) {
    this.userRepository = userRepository;
    this.sellerRepository = sellerRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailUniquenessValidator = emailUniquenessValidator;
  }

  @Override
  @Transactional
  public Seller execute(
      String fullName,
      String email,
      String rawPassword,
      String identityDocument,
      String businessName,
      String taxId,
      String contactPhone) {
    emailUniquenessValidator.ensureAvailable(email);
    if (rawPassword == null || rawPassword.isBlank()) throw new InvalidPasswordException();
    String hash = passwordEncoder.encode(rawPassword);
    User user = User.create(fullName, email, hash, User.Role.SELLER, identityDocument);
    user = userRepository.save(user);
    return sellerRepository.save(Seller.create(user.getId(), businessName, taxId, contactPhone));
  }
}
