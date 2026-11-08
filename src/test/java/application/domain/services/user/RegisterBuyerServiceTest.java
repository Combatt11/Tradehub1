package application.domain.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.exceptions.InvalidPasswordException;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterBuyerService")
class RegisterBuyerServiceTest {

  @Mock private UserRepositoryPort userRepository;
  @Mock private BuyerRepositoryPort buyerRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private EmailUniquenessValidator emailUniquenessValidator;

  @InjectMocks private RegisterBuyerService service;

  @Test
  @DisplayName("rejects a blank password")
  void rejectsBlankPassword() {
    assertThatThrownBy(() -> service.execute("Ana", "ana@example.com", " ", "DOC-1", "Calle 1"))
        .isInstanceOf(InvalidPasswordException.class)
        .hasMessage("Password is required");

    verify(emailUniquenessValidator).ensureAvailable("ana@example.com");
    verifyNoInteractions(userRepository, buyerRepository);
  }

  @Test
  @DisplayName("registers the user and its buyer profile")
  void registersBuyer() {
    when(passwordEncoder.encode("secret")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User user = service.execute("Ana", "ana@example.com", "secret", "DOC-1", "Calle 1");

    assertThat(user.getPasswordHash()).isEqualTo("hashed");
    assertThat(user.getRole()).isEqualTo(User.Role.BUYER);
    verify(buyerRepository).save(any(Buyer.class));
  }
}
