package application.domain.exceptions;

/** Raised when a user that is already active is activated again. */
public class UserAlreadyActiveException extends DomainException {
  public UserAlreadyActiveException(String userId) {
    super("User is already active: " + userId);
  }
}
