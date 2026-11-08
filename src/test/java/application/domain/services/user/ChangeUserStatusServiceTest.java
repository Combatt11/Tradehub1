package application.domain.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.exceptions.InvalidUserStatusException;
import application.domain.exceptions.UserAlreadyActiveException;
import application.domain.exceptions.UserNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeUserStatusService")
class ChangeUserStatusServiceTest {

  private static final String USER_ID = "user-1";

  @Mock private UserRepositoryPort userRepository;

  @InjectMocks private ChangeUserStatusService service;

  private static User activeUser() {
    return User.create("Ana", "ana@example.com", "hash", User.Role.BUYER, "DOC-1");
  }

  @Test
  @DisplayName("throws when the target status is missing")
  void throwsWhenStatusIsMissing() {
    assertThatThrownBy(() -> service.changeUserStatus(USER_ID, null))
        .isInstanceOf(InvalidUserStatusException.class)
        .hasMessage("User status is required");

    verifyNoInteractions(userRepository);
  }

  @Test
  @DisplayName("throws when the user does not exist")
  void throwsWhenUserNotFound() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.changeUserStatus(USER_ID, User.Status.BLOCKED))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessage("User not found: user-1");
  }

  @Test
  @DisplayName("applies the requested status")
  void changesStatus() {
    User user = activeUser();
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.save(user)).thenReturn(user);

    User result = service.changeUserStatus(USER_ID, User.Status.BLOCKED);

    assertThat(result.getStatus()).isEqualTo(User.Status.BLOCKED);
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("rejects a transition to the status the user already has")
  void rejectsRepeatedStatus() {
    User user = activeUser();
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.changeUserStatus(USER_ID, User.Status.ACTIVE))
        .isInstanceOf(UserAlreadyActiveException.class)
        .hasMessageContaining("User is already active");
  }
}
