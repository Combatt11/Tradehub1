package application.domain.exceptions;

/** Raised when a password is missing or blank. */
public class InvalidPasswordException extends DomainException {
  public InvalidPasswordException() {
    super("Password is required");
  }
}
