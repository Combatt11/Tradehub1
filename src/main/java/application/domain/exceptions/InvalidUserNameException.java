package application.domain.exceptions;

/** Raised when a user is created or renamed without a full name. */
public class InvalidUserNameException extends DomainException {
  public InvalidUserNameException() {
    super("User full name is required");
  }
}
