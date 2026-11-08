package application.domain.exceptions;

/** Raised when a user that is already blocked is blocked again. */
public class UserAlreadyBlockedException extends DomainException {
  public UserAlreadyBlockedException(String userId) {
    super("User is already blocked: " + userId);
  }
}
