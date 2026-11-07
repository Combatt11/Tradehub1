package application.domain.services.user;

import application.domain.exceptions.DuplicateEmailException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterBuyerService {
    private final UserRepositoryPort userRepository;
    private final BuyerRepositoryPort buyerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterBuyerService(UserRepositoryPort userRepository, BuyerRepositoryPort buyerRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buyerRepository = buyerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User execute(String fullName, String email, String rawPassword,
                        String identityDocument, String primaryAddress) {
        if (userRepository.existsByEmail(email)) throw new DuplicateEmailException(email);
        String hash = passwordEncoder.encode(rawPassword);
        User user = User.create(fullName, email, hash, User.Role.BUYER, identityDocument);
        user = userRepository.save(user);
        buyerRepository.save(Buyer.create(user.getId(), primaryAddress));
        return user;
    }
}
