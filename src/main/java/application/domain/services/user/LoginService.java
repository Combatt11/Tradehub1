package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive()) throw new InvalidCredentialsException();
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) throw new InvalidCredentialsException();
        return user;
    }
}
