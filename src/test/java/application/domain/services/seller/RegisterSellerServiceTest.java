package application.domain.services.seller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import application.domain.exceptions.InvalidPasswordException;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.user.EmailUniquenessValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService")
class RegisterSellerServiceTest {

  @Mock private UserRepositoryPort userRepository;
  @Mock private SellerRepositoryPort sellerRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private EmailUniquenessValidator emailUniquenessValidator;

  @InjectMocks private RegisterSellerService service;

  @Test
  @DisplayName("rejects a blank password")
  void rejectsBlankPassword() {
    assertThatThrownBy(
            () ->
                service.execute(
                    "Acme", "acme@example.com", null, "DOC-1", "Acme", "TAX-1", "3001234567"))
        .isInstanceOf(InvalidPasswordException.class)
        .hasMessage("Password is required");

    verify(emailUniquenessValidator).ensureAvailable("acme@example.com");
    verifyNoInteractions(userRepository, sellerRepository);
  }
}
