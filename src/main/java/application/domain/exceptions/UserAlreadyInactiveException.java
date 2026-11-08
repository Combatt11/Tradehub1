package application.domain.exceptions;

/** Raised when a user that is already inactive is deactivated again. */
public class UserAlreadyInactiveException extends DomainException {
  public UserAlreadyInactiveException(String userId) {
    super("User is already inactive: " + userId);
  }
}
