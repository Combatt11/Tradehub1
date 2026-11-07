package application.domain.services.seller;

import application.domain.exceptions.DuplicateEmailException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSellerService {
    private final UserRepositoryPort userRepository;
    private final SellerRepositoryPort sellerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterSellerService(UserRepositoryPort userRepository, SellerRepositoryPort sellerRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Seller execute(String fullName, String email, String rawPassword, String identityDocument,
                          String businessName, String taxId, String contactPhone) {
        if (userRepository.existsByEmail(email)) throw new DuplicateEmailException(email);
        String hash = passwordEncoder.encode(rawPassword);
        User user = User.create(fullName, email, hash, User.Role.SELLER, identityDocument);
        user = userRepository.save(user);
        return sellerRepository.save(Seller.create(user.getId(), businessName, taxId, contactPhone));
    }
}
