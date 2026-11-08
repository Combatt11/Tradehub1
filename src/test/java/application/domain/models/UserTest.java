package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.InvalidPasswordException;
import application.domain.exceptions.InvalidUserNameException;
import application.domain.exceptions.UserAlreadyActiveException;
import application.domain.exceptions.UserAlreadyBlockedException;
import application.domain.exceptions.UserAlreadyInactiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User")
class UserTest {

  private static User user() {
    return User.create("Ana Buyer", "ana@example.com", "hash", User.Role.BUYER, "DOC-1");
  }

  @Test
  @DisplayName("guards every status transition against repeating the current status")
  void guardsStatusTransitions() {
    User user = user();
    assertThat(user.getStatus()).isEqualTo(User.Status.ACTIVE);

    assertThatThrownBy(user::activate)
        .isInstanceOf(UserAlreadyActiveException.class)
        .hasMessageContaining("User is already active");

    user.block();
    assertThat(user.getStatus()).isEqualTo(User.Status.BLOCKED);
    assertThatThrownBy(user::block)
        .isInstanceOf(UserAlreadyBlockedException.class)
        .hasMessageContaining("User is already blocked");

    user.deactivate();
    assertThat(user.getStatus()).isEqualTo(User.Status.INACTIVE);
    assertThatThrownBy(user::deactivate)
        .isInstanceOf(UserAlreadyInactiveException.class)
        .hasMessageContaining("User is already inactive");

    user.activate();
    assertThat(user.getStatus()).isEqualTo(User.Status.ACTIVE);
  }

  @Test
  @DisplayName("rejects blank names and passwords")
  void rejectsBlankNamesAndPasswords() {
    assertThatThrownBy(() -> User.create(" ", "ana@example.com", "hash", User.Role.BUYER, "DOC-1"))
        .isInstanceOf(InvalidUserNameException.class)
        .hasMessage("User full name is required");

    User user = user();
    assertThatThrownBy(() -> user.setFullName(null)).isInstanceOf(InvalidUserNameException.class);

    assertThatThrownBy(() -> user.changePassword(" "))
        .isInstanceOf(InvalidPasswordException.class)
        .hasMessage("Password is required");

    user.changePassword("new-hash");
    assertThat(user.getPasswordHash()).isEqualTo("new-hash");
  }
}
